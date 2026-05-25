param(
    [switch]$NoExit
)

Set-StrictMode -Version 2.0
$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

[System.Windows.Forms.Application]::EnableVisualStyles()

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$JarFile = Join-Path $ScriptDir 'scaffold4j-cli\target\scaffold4j-cli-1.0.0-SNAPSHOT.jar'

function ConvertTo-CommandLineArgument {
    param([string]$Value)

    if ($null -eq $Value) { return '""' }
    if ($Value -eq '') { return '""' }
    if ($Value -match '[\s"]') {
        return '"' + ($Value -replace '"', '\"') + '"'
    }
    return $Value
}

function New-Label {
    param([string]$Text, [int]$X, [int]$Y, [int]$Width = 130)

    $label = New-Object System.Windows.Forms.Label
    $label.Text = $Text
    $label.Location = New-Object System.Drawing.Point($X, $Y)
    $label.Size = New-Object System.Drawing.Size($Width, 22)
    $label.TextAlign = [System.Drawing.ContentAlignment]::MiddleLeft
    return $label
}

function New-TextBox {
    param([string]$Text, [int]$X, [int]$Y, [int]$Width = 240)

    $box = New-Object System.Windows.Forms.TextBox
    $box.Text = $Text
    $box.Location = New-Object System.Drawing.Point($X, $Y)
    $box.Size = New-Object System.Drawing.Size($Width, 24)
    return $box
}

function New-ComboBox {
    param([string[]]$Items, [string]$Selected, [int]$X, [int]$Y, [int]$Width = 240)

    $combo = New-Object System.Windows.Forms.ComboBox
    $combo.DropDownStyle = [System.Windows.Forms.ComboBoxStyle]::DropDownList
    [void]$combo.Items.AddRange($Items)
    $combo.Location = New-Object System.Drawing.Point($X, $Y)
    $combo.Size = New-Object System.Drawing.Size($Width, 24)
    $combo.SelectedItem = $Selected
    if ($combo.SelectedIndex -lt 0 -and $combo.Items.Count -gt 0) { $combo.SelectedIndex = 0 }
    return $combo
}

function New-CheckedListBox {
    param([string[]]$Items, [string[]]$Checked, [int]$X, [int]$Y, [int]$Width = 240, [int]$Height = 96)

    $list = New-Object System.Windows.Forms.CheckedListBox
    $list.CheckOnClick = $true
    $list.Location = New-Object System.Drawing.Point($X, $Y)
    $list.Size = New-Object System.Drawing.Size($Width, $Height)
    [void]$list.Items.AddRange($Items)
    for ($i = 0; $i -lt $list.Items.Count; $i++) {
        if ($Checked -contains [string]$list.Items[$i]) {
            $list.SetItemChecked($i, $true)
        }
    }
    return $list
}

function Get-CheckedCsv {
    param([System.Windows.Forms.CheckedListBox]$List)

    $values = New-Object System.Collections.Generic.List[string]
    foreach ($item in $List.CheckedItems) { $values.Add([string]$item) }
    return ($values.ToArray() -join ',')
}

function Append-Log {
    param([string]$Message)

    $logBox.AppendText($Message + [Environment]::NewLine)
    $logBox.SelectionStart = $logBox.TextLength
    $logBox.ScrollToCaret()
    [System.Windows.Forms.Application]::DoEvents()
}

function Run-ExternalProcess {
    param(
        [string]$FileName,
        [string[]]$Arguments
    )

    $psi = New-Object System.Diagnostics.ProcessStartInfo
    $psi.FileName = $FileName
    $psi.Arguments = (($Arguments | ForEach-Object { ConvertTo-CommandLineArgument $_ }) -join ' ')
    $psi.WorkingDirectory = $ScriptDir
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true

    Append-Log ("> " + $psi.FileName + " " + $psi.Arguments)

    $process = [System.Diagnostics.Process]::Start($psi)
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    if (-not [string]::IsNullOrWhiteSpace($stdout)) { Append-Log $stdout.TrimEnd() }
    if (-not [string]::IsNullOrWhiteSpace($stderr)) { Append-Log $stderr.TrimEnd() }

    return $process.ExitCode
}

function Ensure-CliJar {
    if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
        if (Test-Path $JarFile) {
            Append-Log '未找到 Maven，使用现有 CLI JAR。'
            return $true
        }
        [System.Windows.Forms.MessageBox]::Show('未找到 Maven，且 CLI JAR 不存在。请安装 Maven 3.9+，或复制 scaffold4j-cli\target\scaffold4j-cli-1.0.0-SNAPSHOT.jar 到当前目录。', '缺少 Maven/JAR', 'OK', 'Error') | Out-Null
        return $false
    }

    Append-Log '正在构建/更新 scaffold4j CLI...'
    $exitCode = Run-ExternalProcess 'cmd.exe' @('/d', '/c', 'mvn', '-q', '-f', (Join-Path $ScriptDir 'pom.xml'), 'clean', 'package', '-DskipTests')
    if ($exitCode -ne 0) {
        [System.Windows.Forms.MessageBox]::Show("构建失败，退出码：$exitCode。请查看日志。", '构建失败', 'OK', 'Error') | Out-Null
        return $false
    }

    return (Test-Path $JarFile)
}

function Update-DependentControls {
    $nacosAddrBox.Enabled = $nacosCheck.Checked
    $nacosNsBox.Enabled = $nacosCheck.Checked

    $dbEnabled = ([string]$dbTypeCombo.SelectedItem) -ne 'h2'
    $dbHostBox.Enabled = $dbEnabled
    $dbPortBox.Enabled = $dbEnabled
    $dbNameBox.Enabled = $dbEnabled
    $dbUserBox.Enabled = $dbEnabled
    $dbPasswordBox.Enabled = $dbEnabled
    if ($dbEnabled -and [string]::IsNullOrWhiteSpace($dbPortBox.Text)) {
        if ([string]$dbTypeCombo.SelectedItem -eq 'mysql') { $dbPortBox.Text = '3306' }
        if ([string]$dbTypeCombo.SelectedItem -eq 'postgresql') { $dbPortBox.Text = '5432' }
    }

    $redisEnabled = ([string]$cacheTypeCombo.SelectedItem) -eq 'redis'
    $redisHostBox.Enabled = $redisEnabled
    $redisPortBox.Enabled = $redisEnabled
    $redisPasswordBox.Enabled = $redisEnabled
    $redisDbBox.Enabled = $redisEnabled

    $mqEnabled = ([string]$mqTypeCombo.SelectedItem) -ne 'none'
    $mqHostBox.Enabled = $mqEnabled
    $mqPortBox.Enabled = $mqEnabled
    $mqUserBox.Enabled = $mqEnabled
    $mqPasswordBox.Enabled = $mqEnabled
    $mqVhostBox.Enabled = ($mqEnabled -and ([string]$mqTypeCombo.SelectedItem) -eq 'rabbitmq')
    $mqGroupBox.Enabled = $mqEnabled
    if ($mqEnabled -and [string]::IsNullOrWhiteSpace($mqPortBox.Text)) {
        if ([string]$mqTypeCombo.SelectedItem -eq 'rabbitmq') { $mqPortBox.Text = '5672' }
        if ([string]$mqTypeCombo.SelectedItem -eq 'rocketmq') { $mqPortBox.Text = '9876' }
        if ([string]$mqTypeCombo.SelectedItem -eq 'kafka') { $mqPortBox.Text = '9092' }
    }
}

function Validate-RequiredInputs {
    if ([string]::IsNullOrWhiteSpace($projectNameBox.Text)) { throw '请输入项目名称。' }
    if ([string]::IsNullOrWhiteSpace($packageBox.Text)) { throw '请输入基础包名。' }
    if ([string]::IsNullOrWhiteSpace((Get-CheckedCsv $providerList))) { throw '请至少选择一个 LLM Provider。' }
    if ([string]::IsNullOrWhiteSpace((Get-CheckedCsv $protocolList))) { throw '请至少选择一个协议。' }
}

function Generate-Project {
    try {
        Validate-RequiredInputs
    } catch {
        [System.Windows.Forms.MessageBox]::Show($_.Exception.Message, '参数不完整', 'OK', 'Warning') | Out-Null
        return
    }

    if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        [System.Windows.Forms.MessageBox]::Show('未找到 Java。请安装 JDK 17+，并设置 JAVA_HOME/PATH。', '缺少 Java', 'OK', 'Error') | Out-Null
        return
    }

    $generateButton.Enabled = $false
    $openOutputButton.Enabled = $false
    try {
        if (-not (Ensure-CliJar)) { return }

        $dbPort = if ([string]::IsNullOrWhiteSpace($dbPortBox.Text)) { '0' } else { $dbPortBox.Text.Trim() }
        $redisPassword = if ($null -eq $redisPasswordBox.Text) { '' } else { $redisPasswordBox.Text }
        $nacosNamespace = if ($null -eq $nacosNsBox.Text) { '' } else { $nacosNsBox.Text }

        $args = New-Object System.Collections.Generic.List[string]
        $args.Add('-jar')
        $args.Add($JarFile)
        $args.Add('generate')
        $args.Add('--name'); $args.Add($projectNameBox.Text.Trim())
        $args.Add('--package'); $args.Add($packageBox.Text.Trim())
        $args.Add('--group-id'); $args.Add($groupIdBox.Text.Trim())
        $args.Add('--artifact-id'); $args.Add($artifactIdBox.Text.Trim())
        $args.Add('--version'); $args.Add($versionBox.Text.Trim())
        $args.Add('--java-version'); $args.Add([string]$javaVersionCombo.SelectedItem)
        $args.Add('--spring-boot-version'); $args.Add($bootVersionBox.Text.Trim())
        $args.Add('--ai-framework'); $args.Add([string]$frameworkCombo.SelectedItem)
        $args.Add('--llm-providers'); $args.Add((Get-CheckedCsv $providerList))
        $args.Add('--protocols'); $args.Add((Get-CheckedCsv $protocolList))
        $args.Add('--features'); $args.Add((Get-CheckedCsv $featureList))
        $args.Add('--vector-store'); $args.Add([string]$vectorStoreCombo.SelectedItem)
        if ($nacosCheck.Checked) { $args.Add('--nacos') }
        $args.Add('--nacos-addr'); $args.Add($nacosAddrBox.Text.Trim())
        $args.Add('--nacos-namespace'); $args.Add($nacosNamespace)
        $args.Add('--db-type'); $args.Add([string]$dbTypeCombo.SelectedItem)
        $args.Add('--db-host'); $args.Add($dbHostBox.Text.Trim())
        $args.Add('--db-port'); $args.Add($dbPort)
        $args.Add('--db-name'); $args.Add($dbNameBox.Text.Trim())
        $args.Add('--db-username'); $args.Add($dbUserBox.Text.Trim())
        $args.Add('--db-password'); $args.Add($dbPasswordBox.Text)
        $args.Add('--orm'); $args.Add([string]$ormCombo.SelectedItem)
        $args.Add('--cache-type'); $args.Add([string]$cacheTypeCombo.SelectedItem)
        $args.Add('--redis-host'); $args.Add($redisHostBox.Text.Trim())
        $args.Add('--redis-port'); $args.Add($redisPortBox.Text.Trim())
        $args.Add('--redis-password'); $args.Add($redisPassword)
        $args.Add('--redis-database'); $args.Add($redisDbBox.Text.Trim())
        $mqType = [string]$mqTypeCombo.SelectedItem
        if ($mqType -ne 'none') {
            $mqPort = if ([string]::IsNullOrWhiteSpace($mqPortBox.Text)) { '0' } else { $mqPortBox.Text.Trim() }
            $args.Add('--mq-type'); $args.Add($mqType)
            $args.Add('--mq-host'); $args.Add($mqHostBox.Text.Trim())
            $args.Add('--mq-port'); $args.Add($mqPort)
            $args.Add('--mq-username'); $args.Add($mqUserBox.Text.Trim())
            $args.Add('--mq-password'); $args.Add($mqPasswordBox.Text)
            $args.Add('--mq-virtual-host'); $args.Add($mqVhostBox.Text)
            $args.Add('--mq-group'); $args.Add($mqGroupBox.Text.Trim())
        } else {
            Append-Log 'MQ 类型为 none，已跳过 MQ host/port/auth/group 参数。'
        }
        $args.Add('--output-dir'); $args.Add($outputDirBox.Text.Trim())

        Append-Log '开始生成项目...'
        $exitCode = Run-ExternalProcess 'java' $args.ToArray()
        if ($exitCode -eq 0) {
            $lastOutputPath = Join-Path $outputDirBox.Text.Trim() $artifactIdBox.Text.Trim()
            $openOutputButton.Tag = $lastOutputPath
            $openOutputButton.Enabled = $true
            Append-Log "生成完成：$lastOutputPath"
            [System.Windows.Forms.MessageBox]::Show("项目生成完成：`n$lastOutputPath", '完成', 'OK', 'Information') | Out-Null
        } else {
            [System.Windows.Forms.MessageBox]::Show("生成失败，退出码：$exitCode。请查看日志。", '失败', 'OK', 'Error') | Out-Null
        }
    } catch {
        Append-Log ('ERROR: ' + $_.Exception.Message)
        [System.Windows.Forms.MessageBox]::Show($_.Exception.Message, '错误', 'OK', 'Error') | Out-Null
    } finally {
        $generateButton.Enabled = $true
    }
}

$form = New-Object System.Windows.Forms.Form
$form.Text = 'scaffold4j 可视化项目初始化引导程序'
$form.Size = New-Object System.Drawing.Size(980, 740)
$form.StartPosition = 'CenterScreen'
$form.MinimumSize = New-Object System.Drawing.Size(920, 660)

$title = New-Object System.Windows.Forms.Label
$title.Text = 'scaffold4j Windows 可视化项目初始化引导程序'
$title.Font = New-Object System.Drawing.Font('Microsoft YaHei UI', 14, [System.Drawing.FontStyle]::Bold)
$title.Location = New-Object System.Drawing.Point(16, 12)
$title.Size = New-Object System.Drawing.Size(920, 32)
$form.Controls.Add($title)

$tabs = New-Object System.Windows.Forms.TabControl
$tabs.Location = New-Object System.Drawing.Point(16, 52)
$tabs.Size = New-Object System.Drawing.Size(930, 420)
$tabs.Anchor = 'Top,Left,Right'
$form.Controls.Add($tabs)

$basicTab = New-Object System.Windows.Forms.TabPage
$basicTab.Text = '基础信息'
$tabs.TabPages.Add($basicTab)

$basicTab.Controls.Add((New-Label '项目名称' 24 30))
$projectNameBox = New-TextBox 'my-ai-app' 160 30 260
$basicTab.Controls.Add($projectNameBox)
$basicTab.Controls.Add((New-Label '基础包名' 480 30))
$packageBox = New-TextBox 'com.example.ai' 620 30 240
$basicTab.Controls.Add($packageBox)

$basicTab.Controls.Add((New-Label 'Maven groupId' 24 76))
$groupIdBox = New-TextBox 'com.example.ai' 160 76 260
$basicTab.Controls.Add($groupIdBox)
$basicTab.Controls.Add((New-Label 'Maven artifactId' 480 76))
$artifactIdBox = New-TextBox 'my-ai-app' 620 76 240
$basicTab.Controls.Add($artifactIdBox)

$basicTab.Controls.Add((New-Label '版本' 24 122))
$versionBox = New-TextBox '1.0.0-SNAPSHOT' 160 122 260
$basicTab.Controls.Add($versionBox)
$basicTab.Controls.Add((New-Label '输出目录' 480 122))
$outputDirBox = New-TextBox '.' 620 122 200
$basicTab.Controls.Add($outputDirBox)
$browseButton = New-Object System.Windows.Forms.Button
$browseButton.Text = '浏览...'
$browseButton.Location = New-Object System.Drawing.Point(828, 120)
$browseButton.Size = New-Object System.Drawing.Size(70, 28)
$basicTab.Controls.Add($browseButton)

$syncNamesCheck = New-Object System.Windows.Forms.CheckBox
$syncNamesCheck.Text = '项目名称变化时同步 artifactId，包名变化时同步 groupId'
$syncNamesCheck.Checked = $true
$syncNamesCheck.Location = New-Object System.Drawing.Point(160, 170)
$syncNamesCheck.Size = New-Object System.Drawing.Size(430, 24)
$basicTab.Controls.Add($syncNamesCheck)

$aiTab = New-Object System.Windows.Forms.TabPage
$aiTab.Text = 'AI 与协议'
$tabs.TabPages.Add($aiTab)

$aiTab.Controls.Add((New-Label 'Java 版本' 24 28))
$javaVersionCombo = New-ComboBox @('17', '21') '17' 160 28 180
$aiTab.Controls.Add($javaVersionCombo)
$aiTab.Controls.Add((New-Label 'Spring Boot 版本' 420 28))
$bootVersionBox = New-TextBox '3.5.0' 560 28 180
$aiTab.Controls.Add($bootVersionBox)

$aiTab.Controls.Add((New-Label 'AI 框架' 24 74))
$frameworkCombo = New-ComboBox @('spring-ai', 'spring-ai-alibaba', 'langchain4j', 'both') 'spring-ai' 160 74 180
$aiTab.Controls.Add($frameworkCombo)
$aiTab.Controls.Add((New-Label '向量数据库' 420 74))
$vectorStoreCombo = New-ComboBox @('pgvector', 'milvus', 'chroma', 'pinecone', 'elasticsearch', 'redis', 'weaviate', 'qdrant', 'simple') 'pgvector' 560 74 180
$aiTab.Controls.Add($vectorStoreCombo)

$aiTab.Controls.Add((New-Label 'LLM Providers' 24 122))
$providerList = New-CheckedListBox @('openai', 'ollama', 'anthropic', 'deepseek', 'zhipuai', 'vertex-ai', 'azure-openai', 'bedrock', 'qwen', 'moonshot', 'doubao') @('openai') 160 122 220 180
$aiTab.Controls.Add($providerList)

$aiTab.Controls.Add((New-Label '协议' 420 122))
$protocolList = New-CheckedListBox @('rest', 'mcp', 'a2a', 'acp') @('rest') 560 122 180 96
$aiTab.Controls.Add($protocolList)

$aiTab.Controls.Add((New-Label '功能特性' 420 236))
$featureList = New-CheckedListBox @('memory', 'rag', 'sse', 'websocket') @() 560 236 180 96
$aiTab.Controls.Add($featureList)

$infraTab = New-Object System.Windows.Forms.TabPage
$infraTab.Text = '中间件配置'
$tabs.TabPages.Add($infraTab)

$nacosCheck = New-Object System.Windows.Forms.CheckBox
$nacosCheck.Text = '启用 Nacos'
$nacosCheck.Location = New-Object System.Drawing.Point(24, 26)
$nacosCheck.Size = New-Object System.Drawing.Size(120, 24)
$infraTab.Controls.Add($nacosCheck)
$infraTab.Controls.Add((New-Label 'Nacos 地址' 160 26 90))
$nacosAddrBox = New-TextBox 'localhost:8848' 250 26 180
$infraTab.Controls.Add($nacosAddrBox)
$infraTab.Controls.Add((New-Label '命名空间' 470 26 80))
$nacosNsBox = New-TextBox '' 550 26 180
$infraTab.Controls.Add($nacosNsBox)

$infraTab.Controls.Add((New-Label '数据库' 24 78))
$dbTypeCombo = New-ComboBox @('h2', 'mysql', 'postgresql') 'h2' 160 78 160
$infraTab.Controls.Add($dbTypeCombo)
$infraTab.Controls.Add((New-Label 'ORM' 360 78 70))
$ormCombo = New-ComboBox @('mybatis-plus', 'jpa') 'mybatis-plus' 430 78 160
$infraTab.Controls.Add($ormCombo)
$infraTab.Controls.Add((New-Label 'DB Host' 24 120))
$dbHostBox = New-TextBox 'localhost' 160 120 160
$infraTab.Controls.Add($dbHostBox)
$infraTab.Controls.Add((New-Label 'DB Port' 360 120 70))
$dbPortBox = New-TextBox '0' 430 120 80
$infraTab.Controls.Add($dbPortBox)
$infraTab.Controls.Add((New-Label 'DB Name' 550 120 70))
$dbNameBox = New-TextBox 'my-ai-app' 620 120 160
$infraTab.Controls.Add($dbNameBox)
$infraTab.Controls.Add((New-Label 'DB 用户名' 24 162))
$dbUserBox = New-TextBox 'root' 160 162 160
$infraTab.Controls.Add($dbUserBox)
$infraTab.Controls.Add((New-Label 'DB 密码' 360 162 70))
$dbPasswordBox = New-TextBox 'root' 430 162 160
$infraTab.Controls.Add($dbPasswordBox)

$infraTab.Controls.Add((New-Label '缓存' 24 214))
$cacheTypeCombo = New-ComboBox @('none', 'redis', 'caffeine') 'none' 160 214 160
$infraTab.Controls.Add($cacheTypeCombo)
$infraTab.Controls.Add((New-Label 'Redis Host' 360 214 80))
$redisHostBox = New-TextBox 'localhost' 450 214 160
$infraTab.Controls.Add($redisHostBox)
$infraTab.Controls.Add((New-Label 'Redis Port' 650 214 80))
$redisPortBox = New-TextBox '6379' 740 214 80
$infraTab.Controls.Add($redisPortBox)
$infraTab.Controls.Add((New-Label 'Redis 密码' 360 256 80))
$redisPasswordBox = New-TextBox '' 450 256 160
$infraTab.Controls.Add($redisPasswordBox)
$infraTab.Controls.Add((New-Label 'Redis DB' 650 256 80))
$redisDbBox = New-TextBox '0' 740 256 80
$infraTab.Controls.Add($redisDbBox)

$mqTab = New-Object System.Windows.Forms.TabPage
$mqTab.Text = '消息队列'
$tabs.TabPages.Add($mqTab)

$mqTab.Controls.Add((New-Label 'MQ 类型' 24 34))
$mqTypeCombo = New-ComboBox @('none', 'rabbitmq', 'rocketmq', 'kafka') 'none' 160 34 180
$mqTab.Controls.Add($mqTypeCombo)
$mqTab.Controls.Add((New-Label 'MQ Host' 400 34))
$mqHostBox = New-TextBox 'localhost' 540 34 180
$mqTab.Controls.Add($mqHostBox)
$mqTab.Controls.Add((New-Label 'MQ Port' 24 82))
$mqPortBox = New-TextBox '0' 160 82 180
$mqTab.Controls.Add($mqPortBox)
$mqTab.Controls.Add((New-Label 'MQ 用户名' 400 82))
$mqUserBox = New-TextBox 'guest' 540 82 180
$mqTab.Controls.Add($mqUserBox)
$mqTab.Controls.Add((New-Label 'MQ 密码' 24 130))
$mqPasswordBox = New-TextBox 'guest' 160 130 180
$mqTab.Controls.Add($mqPasswordBox)
$mqTab.Controls.Add((New-Label 'RabbitMQ vhost' 400 130))
$mqVhostBox = New-TextBox '/' 540 130 180
$mqTab.Controls.Add($mqVhostBox)
$mqTab.Controls.Add((New-Label '消费者组' 24 178))
$mqGroupBox = New-TextBox 'scaffold4j-consumer' 160 178 260
$mqTab.Controls.Add($mqGroupBox)

$logLabel = New-Object System.Windows.Forms.Label
$logLabel.Text = '执行日志'
$logLabel.Location = New-Object System.Drawing.Point(16, 482)
$logLabel.Size = New-Object System.Drawing.Size(120, 22)
$form.Controls.Add($logLabel)

$logBox = New-Object System.Windows.Forms.TextBox
$logBox.Multiline = $true
$logBox.ScrollBars = 'Vertical'
$logBox.ReadOnly = $true
$logBox.Font = New-Object System.Drawing.Font('Consolas', 9)
$logBox.Location = New-Object System.Drawing.Point(16, 506)
$logBox.Size = New-Object System.Drawing.Size(930, 130)
$logBox.Anchor = 'Top,Left,Right,Bottom'
$form.Controls.Add($logBox)

$generateButton = New-Object System.Windows.Forms.Button
$generateButton.Text = '生成项目'
$generateButton.Location = New-Object System.Drawing.Point(16, 650)
$generateButton.Size = New-Object System.Drawing.Size(110, 34)
$generateButton.Anchor = 'Left,Bottom'
$form.Controls.Add($generateButton)

$openOutputButton = New-Object System.Windows.Forms.Button
$openOutputButton.Text = '打开输出目录'
$openOutputButton.Location = New-Object System.Drawing.Point(142, 650)
$openOutputButton.Size = New-Object System.Drawing.Size(120, 34)
$openOutputButton.Enabled = $false
$openOutputButton.Anchor = 'Left,Bottom'
$form.Controls.Add($openOutputButton)

$clearLogButton = New-Object System.Windows.Forms.Button
$clearLogButton.Text = '清空日志'
$clearLogButton.Location = New-Object System.Drawing.Point(278, 650)
$clearLogButton.Size = New-Object System.Drawing.Size(100, 34)
$clearLogButton.Anchor = 'Left,Bottom'
$form.Controls.Add($clearLogButton)

$closeButton = New-Object System.Windows.Forms.Button
$closeButton.Text = '关闭'
$closeButton.Location = New-Object System.Drawing.Point(846, 650)
$closeButton.Size = New-Object System.Drawing.Size(100, 34)
$closeButton.Anchor = 'Right,Bottom'
$form.Controls.Add($closeButton)

$projectNameBox.Add_TextChanged({
    if ($syncNamesCheck.Checked) {
        $artifactIdBox.Text = $projectNameBox.Text
        $dbNameBox.Text = $projectNameBox.Text
    }
})
$packageBox.Add_TextChanged({
    if ($syncNamesCheck.Checked) { $groupIdBox.Text = $packageBox.Text }
})
$browseButton.Add_Click({
    $dialog = New-Object System.Windows.Forms.FolderBrowserDialog
    $dialog.Description = '选择项目输出目录'
    if ($dialog.ShowDialog($form) -eq [System.Windows.Forms.DialogResult]::OK) {
        $outputDirBox.Text = $dialog.SelectedPath
    }
})
$nacosCheck.Add_CheckedChanged({ Update-DependentControls })
$dbTypeCombo.Add_SelectedIndexChanged({
    if ([string]$dbTypeCombo.SelectedItem -eq 'h2') { $dbPortBox.Text = '0' }
    if ([string]$dbTypeCombo.SelectedItem -eq 'mysql') { $dbPortBox.Text = '3306' }
    if ([string]$dbTypeCombo.SelectedItem -eq 'postgresql') { $dbPortBox.Text = '5432' }
    Update-DependentControls
})
$cacheTypeCombo.Add_SelectedIndexChanged({ Update-DependentControls })
$mqTypeCombo.Add_SelectedIndexChanged({
    if ([string]$mqTypeCombo.SelectedItem -eq 'none') { $mqPortBox.Text = '0' }
    if ([string]$mqTypeCombo.SelectedItem -eq 'rabbitmq') { $mqPortBox.Text = '5672' }
    if ([string]$mqTypeCombo.SelectedItem -eq 'rocketmq') { $mqPortBox.Text = '9876' }
    if ([string]$mqTypeCombo.SelectedItem -eq 'kafka') { $mqPortBox.Text = '9092' }
    Update-DependentControls
})
$generateButton.Add_Click({ Generate-Project })
$openOutputButton.Add_Click({
    if ($openOutputButton.Tag -and (Test-Path ([string]$openOutputButton.Tag))) {
        Start-Process explorer.exe ([string]$openOutputButton.Tag)
    }
})
$clearLogButton.Add_Click({ $logBox.Clear() })
$closeButton.Add_Click({ $form.Close() })

Update-DependentControls
Append-Log '准备就绪。请填写配置后点击“生成项目”。'

[void]$form.ShowDialog()

if ($NoExit) {
    Read-Host '按 Enter 退出'
}
