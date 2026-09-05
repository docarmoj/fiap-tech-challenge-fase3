param(
    [string]$InputFile = "Relatorio Tecnico - CareHub API - Fase 3.md",
    [string]$HtmlFile = "Relatorio Tecnico - CareHub API - Fase 3.html",
    [string]$PdfFile = "Relatorio Tecnico - CareHub API - Fase 3.pdf"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$sessionTools = "C:\Users\docar\.copilot\session-state\63972b67-5062-4f3c-b4bc-198d90828b3c\files"
$pandocExe = Join-Path $sessionTools "pandoc\pandoc-3.6.4\pandoc.exe"
$weasyprintExe = Join-Path $sessionTools "weasyprint\dist\weasyprint.exe"
$cssFile = Join-Path $repoRoot "scripts\report-style.css"

$inputPath = Join-Path $repoRoot $InputFile
$htmlPath = Join-Path $repoRoot $HtmlFile
$pdfPath = Join-Path $repoRoot $PdfFile
$cssUri = "file:///" + ($cssFile -replace "\\", "/")

if (-not (Test-Path $inputPath)) {
    throw "Arquivo Markdown não encontrado: $inputPath"
}

if (-not (Test-Path $pandocExe)) {
    throw "Pandoc não encontrado em $pandocExe"
}

if (-not (Test-Path $weasyprintExe)) {
    throw "WeasyPrint não encontrado em $weasyprintExe"
}

& $pandocExe `
    $inputPath `
    --standalone `
    --toc `
    --css $cssUri `
    --metadata title="Relatorio Tecnico - CareHub API - Fase 3" `
    -o $htmlPath

if ($LASTEXITCODE -ne 0) {
    throw "Falha ao gerar HTML."
}

& $weasyprintExe $htmlPath $pdfPath

if ($LASTEXITCODE -ne 0) {
    throw "Falha ao gerar PDF."
}

Write-Host "HTML gerado em: $htmlPath"
Write-Host "PDF gerado em:  $pdfPath"
