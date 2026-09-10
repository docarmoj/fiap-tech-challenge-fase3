param(
    [string]$InputFile = "Relatorio Tecnico - CareHub API - Fase 3.md",
    [string]$HtmlFile = "Relatorio Tecnico - CareHub API - Fase 3.html",
    [string]$PdfFile = "Relatorio Tecnico - CareHub API - Fase 3.pdf"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$cssFile = Join-Path $repoRoot "scripts\report-style.css"

$inputPath = Join-Path $repoRoot $InputFile
$htmlPath = Join-Path $repoRoot $HtmlFile
$pdfPath = Join-Path $repoRoot $PdfFile
$cssUri = "file:///" + ($cssFile -replace "\\", "/")

function Find-Executable {
    param(
        [string]$CommandName,
        [string]$FileName
    )

    $command = Get-Command $CommandName -ErrorAction SilentlyContinue
    if ($command -and $command.Source) {
        return $command.Source
    }

    $sessionStateRoot = Join-Path $env:USERPROFILE ".copilot\session-state"
    if (Test-Path $sessionStateRoot) {
        $match = Get-ChildItem $sessionStateRoot -Recurse -Filter $FileName -ErrorAction SilentlyContinue |
                Select-Object -First 1 -ExpandProperty FullName
        if ($match) {
            return $match
        }
    }

    return $null
}

$pandocExe = Find-Executable -CommandName "pandoc" -FileName "pandoc.exe"
$weasyprintExe = Find-Executable -CommandName "weasyprint" -FileName "weasyprint.exe"

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

Write-Host "Relatorio tecnico HTML gerado em: $htmlPath"
Write-Host "Relatorio tecnico PDF gerado em:  $pdfPath"
