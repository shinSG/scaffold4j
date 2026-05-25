# Jenkins API 调用文档

Jenkins 提供 Remote Access API，可通过 HTTP/REST 方式调用，用于查询任务、触发构建、查看队列、获取日志、停止构建等自动化场景。

## 1. 基础地址

假设 Jenkins 地址为：

```text
http://localhost:8080
```

Jenkins API 入口：

```text
http://localhost:8080/api/
```

常用返回格式：

```text
/api/json
/api/xml
/api/python
```

示例：

```bash
curl "http://localhost:8080/api/json"
```

## 2. 认证方式

推荐使用：

```text
用户名 + API Token
```

API Token 获取路径通常为：

```text
Jenkins 页面右上角用户头像 -> Configure -> API Token -> Add new Token
```

curl 示例：

```bash
curl -u "user:api_token" "http://localhost:8080/api/json"
```

不推荐在自动化脚本中使用 Jenkins 登录密码。

## 3. CSRF Crumb

如果 Jenkins 开启了 CSRF 防护，POST 请求可能需要携带 crumb。

获取 crumb：

```bash
curl -u "user:api_token" \
  "http://localhost:8080/crumbIssuer/api/json"
```

返回示例：

```json
{
  "_class": "hudson.security.csrf.DefaultCrumbIssuer",
  "crumb": "xxxxxx",
  "crumbRequestField": "Jenkins-Crumb"
}
```

携带 crumb 调用 POST：

```bash
curl -X POST -u "user:api_token" \
  -H "Jenkins-Crumb: xxxxxx" \
  "http://localhost:8080/job/my-job/build"
```

## 4. 查询 Jenkins 信息

### 4.1 查询 Jenkins 根信息

```bash
curl -u "user:api_token" \
  "http://localhost:8080/api/json"
```

### 4.2 查询所有 Job

```bash
curl -u "user:api_token" \
  "http://localhost:8080/api/json?tree=jobs[name,url,color]"
```

返回字段说明：

| 字段 | 说明 |
| --- | --- |
| `name` | Job 名称 |
| `url` | Job 地址 |
| `color` | Job 状态，例如 `blue`、`red`、`disabled` |

## 5. Job API

### 5.1 获取 Job 信息

```bash
curl -u "user:api_token" \
  "http://localhost:8080/job/my-job/api/json"
```

常用过滤字段：

```bash
curl -u "user:api_token" \
  "http://localhost:8080/job/my-job/api/json?tree=name,url,buildable,lastBuild[number,url,result,timestamp,duration]"
```

### 5.2 获取 Job 配置 XML

```bash
curl -u "user:api_token" \
  "http://localhost:8080/job/my-job/config.xml"
```

### 5.3 更新 Job 配置 XML

```bash
curl -X POST -u "user:api_token" \
  -H "Content-Type: application/xml" \
  --data-binary @config.xml \
  "http://localhost:8080/job/my-job/config.xml"
```

### 5.4 启用 Job

```bash
curl -X POST -u "user:api_token" \
  "http://localhost:8080/job/my-job/enable"
```

### 5.5 禁用 Job

```bash
curl -X POST -u "user:api_token" \
  "http://localhost:8080/job/my-job/disable"
```

### 5.6 删除 Job

```bash
curl -X POST -u "user:api_token" \
  "http://localhost:8080/job/my-job/doDelete"
```

## 6. 构建 API

### 6.1 触发普通构建

```bash
curl -X POST -u "user:api_token" \
  "http://localhost:8080/job/my-job/build"
```

调用成功后，通常返回 HTTP `201` 或 `302`，并在响应头 `Location` 中返回队列地址。

### 6.2 触发带参数构建

```bash
curl -X POST -u "user:api_token" \
  "http://localhost:8080/job/my-job/buildWithParameters?branch=main&env=dev"
```

也可以使用表单参数：

```bash
curl -X POST -u "user:api_token" \
  --data-urlencode "branch=main" \
  --data-urlencode "env=dev" \
  "http://localhost:8080/job/my-job/buildWithParameters"
```

### 6.3 停止构建

```bash
curl -X POST -u "user:api_token" \
  "http://localhost:8080/job/my-job/1/stop"
```

### 6.4 强制终止构建

```bash
curl -X POST -u "user:api_token" \
  "http://localhost:8080/job/my-job/1/term"
```

### 6.5 Kill 构建

```bash
curl -X POST -u "user:api_token" \
  "http://localhost:8080/job/my-job/1/kill"
```

## 7. Build API

### 7.1 获取构建详情

```bash
curl -u "user:api_token" \
  "http://localhost:8080/job/my-job/1/api/json"
```

常用字段过滤：

```bash
curl -u "user:api_token" \
  "http://localhost:8080/job/my-job/1/api/json?tree=number,url,result,building,timestamp,duration,estimatedDuration,description,actions[parameters[name,value]]"
```

关键字段说明：

| 字段 | 说明 |
| --- | --- |
| `number` | 构建编号 |
| `building` | 是否正在构建 |
| `result` | 构建结果，构建中通常为 `null` |
| `timestamp` | 构建开始时间戳 |
| `duration` | 构建耗时 |
| `estimatedDuration` | 预计耗时 |

### 7.2 获取控制台日志

```bash
curl -u "user:api_token" \
  "http://localhost:8080/job/my-job/1/consoleText"
```

### 7.3 获取 HTML 控制台日志

```bash
curl -u "user:api_token" \
  "http://localhost:8080/job/my-job/1/console"
```

### 7.4 渐进式获取日志

适用于实时日志拉取。

```bash
curl -u "user:api_token" \
  "http://localhost:8080/job/my-job/1/logText/progressiveText?start=0"
```

响应头常用字段：

| 响应头 | 说明 |
| --- | --- |
| `X-Text-Size` | 下一次读取起始位置 |
| `X-More-Data` | 是否还有更多日志 |

下一次请求：

```bash
curl -u "user:api_token" \
  "http://localhost:8080/job/my-job/1/logText/progressiveText?start=<X-Text-Size>"
```

### 7.5 获取测试报告

```bash
curl -u "user:api_token" \
  "http://localhost:8080/job/my-job/1/testReport/api/json"
```

## 8. Queue API

### 8.1 查看构建队列

```bash
curl -u "user:api_token" \
  "http://localhost:8080/queue/api/json"
```

### 8.2 查询队列项

触发构建后，如果响应头 `Location` 为：

```text
http://localhost:8080/queue/item/123/
```

则查询：

```bash
curl -u "user:api_token" \
  "http://localhost:8080/queue/item/123/api/json"
```

当队列项进入实际构建后，返回中通常会出现：

```json
{
  "executable": {
    "number": 1,
    "url": "http://localhost:8080/job/my-job/1/"
  }
}
```

### 8.3 取消队列项

```bash
curl -X POST -u "user:api_token" \
  "http://localhost:8080/queue/cancelItem?id=123"
```

## 9. Folder / 多级 Job 路径

如果 Job 在 Folder 中，URL 需要逐级添加 `/job/`。

示例：

```text
folder-a/folder-b/my-job
```

对应 API：

```text
http://localhost:8080/job/folder-a/job/folder-b/job/my-job/api/json
```

触发构建：

```bash
curl -X POST -u "user:api_token" \
  "http://localhost:8080/job/folder-a/job/folder-b/job/my-job/build"
```

## 10. Blue Ocean API

如果安装了 Blue Ocean 插件，可以使用 Blue Ocean API。

### 10.1 查询 Pipeline 运行记录

```bash
curl -u "user:api_token" \
  "http://localhost:8080/blue/rest/organizations/jenkins/pipelines/my-job/runs/"
```

### 10.2 查询某次运行

```bash
curl -u "user:api_token" \
  "http://localhost:8080/blue/rest/organizations/jenkins/pipelines/my-job/runs/1/"
```

### 10.3 查询 Pipeline 节点

```bash
curl -u "user:api_token" \
  "http://localhost:8080/blue/rest/organizations/jenkins/pipelines/my-job/runs/1/nodes/"
```

### 10.4 查询节点日志

```bash
curl -u "user:api_token" \
  "http://localhost:8080/blue/rest/organizations/jenkins/pipelines/my-job/runs/1/nodes/3/log/"
```

## 11. 创建 Job

### 11.1 使用 config.xml 创建 Freestyle Job

```bash
curl -X POST -u "user:api_token" \
  -H "Content-Type: application/xml" \
  --data-binary @config.xml \
  "http://localhost:8080/createItem?name=my-new-job"
```

### 11.2 在 Folder 中创建 Job

```bash
curl -X POST -u "user:api_token" \
  -H "Content-Type: application/xml" \
  --data-binary @config.xml \
  "http://localhost:8080/job/my-folder/createItem?name=my-new-job"
```

## 12. Jenkins CLI

除了 HTTP API，也可以使用 Jenkins CLI。

下载 CLI：

```bash
curl -O "http://localhost:8080/jnlpJars/jenkins-cli.jar"
```

查看帮助：

```bash
java -jar jenkins-cli.jar -s "http://localhost:8080" -auth "user:api_token" help
```

触发构建：

```bash
java -jar jenkins-cli.jar -s "http://localhost:8080" -auth "user:api_token" build my-job -p branch=main -s -v
```

## 13. Java 调用示例

使用 JDK `HttpClient` 触发带参数构建：

```java
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JenkinsApiExample {
    public static void main(String[] args) throws Exception {
        String baseUrl = "http://localhost:8080";
        String user = "user";
        String token = "api_token";
        String jobName = "my-job";

        String auth = Base64.getEncoder().encodeToString((user + ":" + token).getBytes(StandardCharsets.UTF_8));
        String branch = URLEncoder.encode("main", StandardCharsets.UTF_8);
        String env = URLEncoder.encode("dev", StandardCharsets.UTF_8);

        String url = baseUrl + "/job/" + jobName + "/buildWithParameters?branch=" + branch + "&env=" + env;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + auth)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("status=" + response.statusCode());
        System.out.println("location=" + response.headers().firstValue("Location").orElse(""));
    }
}
```

## 14. Python 调用示例

```python
import requests

base_url = "http://localhost:8080"
user = "user"
token = "api_token"
job_name = "my-job"

resp = requests.post(
    f"{base_url}/job/{job_name}/buildWithParameters",
    auth=(user, token),
    params={"branch": "main", "env": "dev"},
)

print(resp.status_code)
print(resp.headers.get("Location"))
```

## 15. Node.js 调用示例

```javascript
const baseUrl = "http://localhost:8080";
const user = "user";
const token = "api_token";
const jobName = "my-job";

const auth = Buffer.from(`${user}:${token}`).toString("base64");
const url = `${baseUrl}/job/${jobName}/buildWithParameters?branch=main&env=dev`;

const response = await fetch(url, {
  method: "POST",
  headers: {
    Authorization: `Basic ${auth}`,
  },
});

console.log(response.status);
console.log(response.headers.get("location"));
```

## 16. 常见状态码

| 状态码 | 说明 |
| --- | --- |
| `200` | 查询成功 |
| `201` | 创建成功，常见于触发构建后进入队列 |
| `302` | 重定向，某些 Jenkins 操作成功后会返回 |
| `400` | 参数错误 |
| `401` | 未认证或认证失败 |
| `403` | 无权限或缺少 crumb |
| `404` | Job 或资源不存在 |
| `500` | Jenkins 服务端错误 |

## 17. 常见问题

### 17.1 POST 返回 403

可能原因：

- 用户权限不足
- API Token 错误
- Jenkins 开启 CSRF 防护，但没有携带 crumb

### 17.2 Job 名称包含空格或中文

需要 URL 编码。

示例：

```text
my job -> my%20job
```

### 17.3 多级目录 Job 调用失败

Folder 中的 Job 需要使用 `/job/目录名/job/任务名/` 形式，而不是直接拼接路径。

### 17.4 触发构建后如何知道构建编号

流程：

1. 调用 `/build` 或 `/buildWithParameters`
2. 从响应头 `Location` 获取队列项地址
3. 轮询 `/queue/item/{id}/api/json`
4. 等待 `executable.number` 出现
5. 使用该 build number 查询构建详情或日志

## 18. 推荐封装流程

自动化系统中建议封装以下流程：

1. 获取 crumb，如果 Jenkins 未开启 CSRF，可忽略失败
2. 触发构建
3. 获取队列项 URL
4. 轮询队列项直到出现 build number
5. 轮询构建状态
6. 拉取控制台日志
7. 根据 `result` 判断成功或失败

构建状态判断：

| `building` | `result` | 说明 |
| --- | --- | --- |
| `true` | `null` | 构建中 |
| `false` | `SUCCESS` | 构建成功 |
| `false` | `FAILURE` | 构建失败 |
| `false` | `ABORTED` | 构建被中止 |
| `false` | `UNSTABLE` | 构建不稳定 |
