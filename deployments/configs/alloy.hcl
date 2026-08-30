logging {
  level  = "info"
  format = "logfmt"
}

otelcol.receiver.otlp "default" {
  grpc {
    endpoint = "0.0.0.0:4317"
  }

  http {
    endpoint = "0.0.0.0:4318"
  }

  output {
    traces = [otelcol.processor.batch.traces.input]
  }
}


otelcol.processor.batch "traces" {
  output {
    traces = [
        otelcol.exporter.otlp.tempo.input,
        otelcol.exporter.otlphttp.langfuse.input,]
  }
}

otelcol.auth.basic "langfuse" {

    username = sys.env("LANGFUSE_PUBLIC_KEY")
    password = sys.env("LANGFUSE_SECRET_KEY")

}

otelcol.exporter.otlphttp "langfuse" {
  client {
    endpoint = "https://cloud.langfuse.com/api/public/otel"
    auth     = otelcol.auth.basic.langfuse.handler

    headers = {
      "x-langfuse-ingestion-version" = "4",
    }
  }
}

otelcol.exporter.otlp "tempo" {
  client {
    endpoint = "tempo:4317"

    tls {
      insecure = true
    }
  }
}


loki.source.file "services" {
  targets = [
    {
      __path__ = "/var/log/services/agentic-service.log",
      service  = "agentic-service",
    },

  ]

  forward_to = [loki.process.logs.receiver]
}

loki.process "logs" {

  stage.regex {
    expression = "trace=(?P<traceID>[a-f0-9]+) span=(?P<spanID>[a-f0-9]+)"
  }

  stage.structured_metadata {
      values = {
        traceID = "",
        spanID  = "",
      }
    }
  stage.labels {
      values = {
        service = "",
      }
    }

  forward_to = [loki.write.loki.receiver]
}

loki.write "loki" {
  endpoint {
    url = "http://loki:3100/loki/api/v1/push"
  }
}