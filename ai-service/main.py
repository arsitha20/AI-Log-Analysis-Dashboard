from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Dict
from openai import OpenAI
import os

# ---------- OpenAI client ----------
# Make sure OPENAI_API_KEY is in your environment
# e.g. export OPENAI_API_KEY="sk-..."
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

app = FastAPI()


# ---------- Pydantic models ----------

class LogEntry(BaseModel):
    id: int | None = None
    timestamp: str | None = None
    level: str | None = None
    serviceName: str | None = None
    message: str
    rawLine: str | None = None


class AnalyzeRequest(BaseModel):
    logs: List[LogEntry]


class ErrorCluster(BaseModel):
    pattern: str
    count: int
    explanation: str
    suggestedFix: str


class AnalyzeResponse(BaseModel):
    clusters: List[ErrorCluster]
    overallSummary: str


# ---------- Simple rule-based fallback ----------

def simple_rule_based_cluster(logs: List[LogEntry]) -> AnalyzeResponse:
    patterns: Dict[str, Dict[str, any]] = {}

    def add(pattern: str, message: str):
        if pattern not in patterns:
            patterns[pattern] = {"count": 0, "samples": []}
        patterns[pattern]["count"] += 1
        if len(patterns[pattern]["samples"]) < 3:
            patterns[pattern]["samples"].append(message)

    for log in logs:
        msg_lower = log.message.lower()
        if "timeout" in msg_lower:
            add("Timeout errors", log.message)
        elif "database" in msg_lower or "db " in msg_lower:
            add("Database connectivity errors", log.message)
        elif "unauthorized" in msg_lower or "authentication" in msg_lower:
            add("Authentication/authorization errors", log.message)
        elif "nullpointer" in msg_lower or "null pointer" in msg_lower:
            add("Null pointer exceptions", log.message)
        elif "exception" in msg_lower or "error" in msg_lower:
            add("Generic application errors", log.message)
        else:
            add("Other logs", log.message)

    clusters: List[ErrorCluster] = []

    for pattern, data in patterns.items():
        explanation = ""
        suggested_fix = ""

        if pattern == "Timeout errors":
            explanation = "The system is experiencing timeouts, likely due to slow downstream services or network issues."
            suggested_fix = "Investigate slow endpoints, add retries with backoff, and monitor latency of dependent services."
        elif pattern == "Database connectivity errors":
            explanation = "The application is unable to communicate with the database."
            suggested_fix = "Check DB credentials, connection URL, network access, and database health."
        elif pattern == "Authentication/authorization errors":
            explanation = "There are issues with user authentication or permissions."
            suggested_fix = "Verify auth tokens, user roles, and configuration of your identity provider."
        elif pattern == "Null pointer exceptions":
            explanation = "The code is referencing objects that are not initialized."
            suggested_fix = "Add null checks, validate inputs, and improve defensive coding practices."
        elif pattern == "Generic application errors":
            explanation = "The system is throwing various application-level errors."
            suggested_fix = "Review exception stack traces, add logging, and write tests around failing paths."
        else:
            explanation = "These logs do not match common error patterns."
            suggested_fix = "Inspect these log lines manually or enhance the analyzer rules."

        clusters.append(
            ErrorCluster(
                pattern=pattern,
                count=data["count"],
                explanation=explanation,
                suggestedFix=suggested_fix
            )
        )

    summary = f"Analyzed {len(logs)} log entries and detected {len(clusters)} error/issue categories."

    return AnalyzeResponse(
        clusters=clusters,
        overallSummary=summary
    )


# ---------- GPT-based analyzer ----------

def analyze_with_gpt(logs: List[LogEntry]) -> AnalyzeResponse:
    if not logs:
        return AnalyzeResponse(clusters=[], overallSummary="No logs to analyze.")

    text_logs = "\n".join([log.rawLine or log.message for log in logs])

    system_msg = (
        "You are an expert backend/SRE engineer. "
        "You analyze application logs, detect error patterns, and suggest fixes."
    )

    user_prompt = f"""
Analyze the following application logs.

1. Group similar errors into clusters.
2. For each cluster, provide:
   - pattern: short name summarizing the issue
   - count: how many log lines fall into this cluster (integer)
   - explanation: what is going wrong
   - suggestedFix: concrete, practical fix

3. Also provide an overallSummary.

Return ONLY valid JSON in exactly this structure:

{{
  "clusters": [
    {{
      "pattern": "string",
      "count": 3,
      "explanation": "string",
      "suggestedFix": "string"
    }}
  ],
  "overallSummary": "string"
}}

Logs:
{text_logs}
"""

    completion = client.chat.completions.create(
        model="gpt-4o-mini",
        response_format={"type": "json_object"},
        messages=[
            {"role": "system", "content": system_msg},
            {"role": "user", "content": user_prompt},
        ],
        temperature=0.2,
        max_tokens=700,
    )

    import json

    content = completion.choices[0].message.content
    ai_data = json.loads(content)

    clusters = [
        ErrorCluster(
            pattern=c["pattern"],
            count=int(c["count"]),
            explanation=c["explanation"],
            suggestedFix=c["suggestedFix"],
        )
        for c in ai_data.get("clusters", [])
    ]

    overall = ai_data.get("overallSummary", "Log analysis completed.")

    return AnalyzeResponse(clusters=clusters, overallSummary=overall)


# ---------- FastAPI route ----------

@app.post("/analyze", response_model=AnalyzeResponse)
def analyze(request: AnalyzeRequest):
    try:
        return analyze_with_gpt(request.logs)
    except Exception as e:
        # Log error in server console and fall back to rule-based
        print("GPT analysis failed, falling back to rule-based:", repr(e))
        return simple_rule_based_cluster(request.logs)
