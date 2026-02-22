{{/*
Expand the name of the chart.
*/}}
{{- define "pizzaflow.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "pizzaflow.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "pizzaflow.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "pizzaflow.labels" -}}
helm.sh/chart: {{ include "pizzaflow.chart" . }}
{{ include "pizzaflow.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: pizzaflow-ecosystem
{{- end }}

{{/*
Selector labels
*/}}
{{- define "pizzaflow.selectorLabels" -}}
app.kubernetes.io/name: {{ include "pizzaflow.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Service labels for a specific service
*/}}
{{- define "pizzaflow.serviceLabels" -}}
{{ include "pizzaflow.labels" . }}
app.kubernetes.io/component: {{ .serviceName }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "pizzaflow.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "pizzaflow.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Common environment variables for all services
*/}}
{{- define "pizzaflow.commonEnv" -}}
- name: SPRING_PROFILES_ACTIVE
  value: {{ .Values.global.environment | quote }}
- name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
  value: "http://{{ include "pizzaflow.fullname" . }}-discovery-service:8761/eureka/"
- name: SPRING_CLOUD_CONFIG_URI
  value: "http://{{ include "pizzaflow.fullname" . }}-config-service:8888"
- name: SPRING_KAFKA_BOOTSTRAP_SERVERS
  value: "{{ .Release.Name }}-kafka:9092"
- name: SPRING_DATA_REDIS_HOST
  value: "{{ .Release.Name }}-redis-master"
- name: SPRING_DATA_REDIS_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Name }}-redis
      key: redis-password
{{- end }}

{{/*
PostgreSQL JDBC URL
*/}}
{{- define "pizzaflow.postgresUrl" -}}
jdbc:postgresql://{{ .Release.Name }}-postgresql:5432/{{ .database }}
{{- end }}

{{/*
MongoDB URI
*/}}
{{- define "pizzaflow.mongoUri" -}}
mongodb://{{ .Release.Name }}-mongodb:27017/{{ .database }}
{{- end }}

{{/*
Image pull secrets
*/}}
{{- define "pizzaflow.imagePullSecrets" -}}
{{- if .Values.global.imagePullSecrets }}
imagePullSecrets:
{{- range .Values.global.imagePullSecrets }}
  - name: {{ . }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Prometheus annotations
*/}}
{{- define "pizzaflow.prometheusAnnotations" -}}
prometheus.io/scrape: "true"
prometheus.io/port: {{ .port | quote }}
prometheus.io/path: "/actuator/prometheus"
{{- end }}
