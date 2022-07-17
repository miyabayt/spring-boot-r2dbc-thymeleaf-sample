# Spring Boot R2DBC Thymeleaf Sample Application

## 開発環境（IntelliJ）の推奨設定

- bootRunを実行している場合でもビルドされるようにする。（単一ファイルのビルドを実行すると spring-devtools が変更を検知して自動的に再起動されるようになります）
    - Intellij > Ctrl+Shift+A > type Registry... > `compiler.automake.allow.when.app.running`をONにする。
- Windowsの場合は、コンソール出力が文字化けするため、`C:¥Program Files¥JetBrains¥IntelliJ Idea xx.x.x¥bin`の中にある`idea64.exe.vmoptions`
  ファイルに`-Dfile.encoding=UTF-8`を追記する。
- ブラウザにLiveReload機能拡張をインストールする。

## Docker

```bash
$ ./gradlew composeUp
```

## minikube

### on MacOS

```bash
$ # starts a local Kubernetes cluster
$ minikube start --vm-driver="hyperkit"

$ # configure environment to use minikube’s Docker daemon
$ eval $(minikube -p minikube docker-env)

$ # enable a minikube addon
$ minikube addons enable ingress

$ # redis / mysql / mailhog
$ kubectl apply -f k8s
```

### on Windows10 pro

```powershell
C:\> minikube start --vm-driver="hyperv"
C:\> minikube docker-env --shell powershell | Invoke-Expression
C:\> minikube addons enable ingress
C:\> kubectl apply -f k8s
```

### set External URL

minikubeのIPアドレスを確認して、gradle.propertiesに設定する

```bash
$ minikube ip
192.168.64.3

$ vi gradle.properties
---
# set nip.io domain with minikube ip
jkube.domain=192.168.64.3.nip.io
---
```

### Build & Apply

```bash
$ # delete old service
$ ./gradlew k8sUndeploy

$ # apply new service
$ ./gradlew clean k8sApply

$ # check pod, svc
$ kubectl get all

$ # tail the log
$ ./gradlew k8sLog
```

### Check running

```bash
$ # check the ingress resource
$ kubectl get ingress
NAME                                 CLASS   HOSTS                                                    ADDRESS        PORTS   AGE
spring-boot-r2dbc-thymeleaf-sample   nginx   spring-boot-r2dbc-thymeleaf-sample.192.168.64.3.nip.io   192.168.64.3   80      11m

$ # send a request
$ curl http://spring-boot-r2dbc-thymeleaf-sample.192.168.64.3.nip.io/actuator/health
{"status":"UP","groups":["liveness","readiness"]}
```

## 動作確認

### ブラウザでの動作確認

<table>
  <tr>
    <th>URL</th>
    <td>http://spring-boot-r2dbc-thymeleaf-sample.192.168.64.3.nip.io</td>
  </tr>
  <tr>
    <th>メールアドレス</th>
    <td>test@example.com</td>
  </tr>
  <tr>
    <th>パスワード</th>
    <td>passw0rd</td>
  </tr>
</table>

### データベースの確認

```bash
mysql -h 192.168.64.3 -P 30306 -uroot -ppassw0rd spring-boot-r2dbc-thymeleaf-sample

mysql> show tables;
+----------------------------------------------+
| Tables_in_spring-boot-r2dbc-thymeleaf-sample |
+----------------------------------------------+
| code_categories                              |
| codes                                        |
| flyway_schema_history                        |
| holidays                                     |
| mail_templates                               |
| permissions                                  |
| role_permissions                             |
| roles                                        |
| send_mail_queue                              |
| staff_roles                                  |
| staffs                                       |
| upload_files                                 |
| user_roles                                   |
| users                                        |
+----------------------------------------------+
14 rows in set (0.01 sec)
```

### メールの確認

http://192.168.64.3:30825

## 参考情報

| プロジェクト                                                                         | 概要                               |
|:-------------------------------------------------------------------------------|:---------------------------------|
| [JKube](https://www.eclipse.org/jkube/)                                        | k8sへのデプロイを簡略化するプラグイン             |
| [Lombok Project](https://projectlombok.org/)                                   | 定型的なコードを書かなくてもよくする               |
| [Springframework](https://spring.io/projects/spring-framework)                 | Spring Framework（Spring WebFlux） |
| [Spring Security](https://spring.io/projects/spring-security)                  | セキュリティ対策、認証・認可のフレームワーク           |
| [Project Reactor](https://projectreactor.io/)                                  | リアクティブプログラミングのためのライブラリ           |
| [Spring Data R2DBC](https://spring.io/projects/spring-data-r2dbc)              | Reactive O/Rマッパー                 |
| [Flyway](https://flywaydb.org/)                                                | DBマイグレーションツール                    |
| [Thymeleaf](http://www.thymeleaf.org/)                                         | テンプレートエンジン                       |
| [Thymeleaf Layout Dialect](https://ultraq.github.io/thymeleaf-layout-dialect/) | テンプレートをレイアウト化する                  |
| [AdminLTE](https://github.com/ColorlibHQ/AdminLTE/releases/tag/v2.3.8)         | 管理画面テンプレート                       |
| [WebJars](https://www.webjars.org/)                                            | jQueryなどのクライアント側ライブラリをJARとして組み込む |
| [ModelMapper](http://modelmapper.org/)                                         | Beanマッピングライブラリ                   |
| [JUnit5](https://junit.org/junit5/)                                            | テストフレームワーク                       |
| [Mockito](https://site.mockito.org/)                                           | モッキングフレームワーク                     |
| [MailHog](https://github.com/mailhog/MailHog)                                  | ウェブベースのSMTPテスター                  |
