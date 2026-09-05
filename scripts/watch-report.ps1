param(
    [string]$InputFile = "Relatorio Tecnico - CareHub API - Fase 3.md"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$inputPath = Join-Path $repoRoot $InputFile
$buildScript = Join-Path $PSScriptRoot "build-report.ps1"

if (-not (Test-Path $inputPath)) {
    throw "Arquivo Markdown não encontrado: $inputPath"
}

Write-Host "Monitorando alterações em: $inputPath"
Write-Host "Pressione Ctrl+C para encerrar."

$directory = Split-Path -Parent $inputPath
$fileName = Split-Path -Leaf $inputPath
$watcher = New-Object System.IO.FileSystemWatcher $directory, $fileName
$watcher.NotifyFilter = [IO.NotifyFilters]'FileName, LastWrite, Size'
$watcher.EnableRaisingEvents = $true

$action = {
    Start-Sleep -Milliseconds 300
    powershell -ExecutionPolicy Bypass -File $using:buildScript | Out-Host
}

$changed = Register-ObjectEvent $watcher Changed -Action $action
$created = Register-ObjectEvent $watcher Created -Action $action
$renamed = Register-ObjectEvent $watcher Renamed -Action $action

try {
    while ($true) {
        Wait-Event | Out-Null
    }
}
finally {
    Unregister-Event -SourceIdentifier $changed.Name
    Unregister-Event -SourceIdentifier $created.Name
    Unregister-Event -SourceIdentifier $renamed.Name
    $watcher.Dispose()
}
