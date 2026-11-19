# Calculadora da Reforma Tributária

Tela do escritório para consultar as calculadoras oficiais da Receita Federal da reforma tributária do consumo. São duas contas diferentes, cada uma com a própria API:

| Cálculo | O que calcula | API | Tela |
|---|---|---|---|
| Regime regular (Lucro Presumido e Lucro Real) | CBS, IBS e Imposto Seletivo a partir de NCM ou NBS | http://localhost:8080/api | http://localhost:8090/ |
| Simples Nacional | CBS e IBS com as alíquotas efetivas do cliente | http://localhost:8082/api | http://localhost:8090/simples |

A tela fica em `tela-calculadora`. Ela não sobe as APIs: as três aplicações precisam estar rodando ao mesmo tempo, cada uma no seu terminal.

As calculadoras oficiais são estes repositórios:

- https://github.com/ReceitaFederal/calculadora-rtc-regime-regular
- https://github.com/ReceitaFederal/calculadora-rtc-simples-nacional

O passo a passo abaixo vale para um Windows 10 ou 11 que ainda não tem Git, Java nem Maven. Use o PowerShell. A primeira execução precisa de internet. Se `winget` não for reconhecido, instale o "Instalador de Aplicativo" pela Microsoft Store e abra o PowerShell de novo.

## 1. Git

O clone do projeto usa o Git. Se `git --version` responder com um número, pule este passo.

```powershell
winget install --id Git.Git -e --accept-package-agreements --accept-source-agreements
```

Feche o PowerShell e abra outro. O terminal que já estava aberto não enxerga o Git recém-instalado.

## 2. JDK 21

O projeto usa Java 21. Não instale só o Java 8. Este comando instala o Temurin 21:

```powershell
winget install --id EclipseAdoptium.Temurin.21.JDK -e --accept-package-agreements --accept-source-agreements
```

Se o Windows pedir permissão de administrador, aceite.

O `mvnw.cmd` não usa o primeiro `java` do PATH. Ele usa o Java da variável `JAVA_HOME`. Grave essa variável para o usuário e também para a janela atual:

```powershell
$jdk = Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory |
    Where-Object { $_.Name -like "jdk-21*" } |
    Sort-Object Name -Descending |
    Select-Object -First 1
[Environment]::SetEnvironmentVariable("JAVA_HOME", $jdk.FullName, "User")
$env:JAVA_HOME = $jdk.FullName
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
java -version
```

A versão impressa precisa ser 21. Cada terminal novo, aberto depois desse comando, já recebe o `JAVA_HOME`. Um terminal que já estava aberto continua sem ele: rode de novo as duas linhas `$env:JAVA_HOME` e `$env:Path` nessa janela, ou feche e abra outra.

## 3. Maven

Não instale o Maven. Dentro de cada pasta do projeto existe o `mvnw.cmd`. Na primeira execução ele baixa o Maven sozinho, desde que o `JAVA_HOME` aponte para o JDK 21 e a máquina tenha internet. Rode o `mvnw.cmd` sempre de dentro da pasta daquele projeto.

## 4. Clonar

```powershell
cd $env:USERPROFILE\Desktop
git clone https://github.com/Billion-contabilidade/calculadora-reforma-tributaria.git
cd calculadora-reforma-tributaria
git clone https://github.com/ReceitaFederal/calculadora-rtc-regime-regular.git
git clone https://github.com/ReceitaFederal/calculadora-rtc-simples-nacional.git
```

Se o `git clone` das calculadoras disser que a pasta já existe, apague essa pasta e rode o clone outra vez.

A partir daqui são três terminais, e cada um começa na pasta do usuário. Cole isto no começo de cada janela. A segunda linha recupera o `JAVA_HOME` quando o terminal foi aberto antes da instalação do Java:

```powershell
$env:JAVA_HOME = [Environment]::GetEnvironmentVariable("JAVA_HOME", "User")
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
java -version
```

A versão impressa precisa ser 21. A primeira chamada do `mvnw.cmd` em cada pasta demora, porque baixa o Maven e as dependências.

## 5. Regime regular, porta 8080

O perfil `offline` só inicia se o banco SQLite existir. Gere o banco uma vez e deixe a API rodando neste terminal:

```powershell
cd "$env:USERPROFILE\Desktop\calculadora-reforma-tributaria\calculadora-rtc-regime-regular"
New-Item -ItemType Directory -Force calculadora\db | Out-Null
.\mvnw.cmd flyway:migrate "-Dflyway.configFiles=flyway\flyway-pro.conf" --batch-mode --errors
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=offline"
```

A API fica em http://localhost:8080/api e o Swagger em http://localhost:8080/api/swagger-ui.html.

Deixe esse terminal aberto. Em 2026 a API lê as alíquotas do banco. A partir de 2027 ela exige alíquotas nominais e o resultado é simulado.

## 6. Simples Nacional, porta 8082

Os scripts SQL não vêm no repositório do Simples Nacional. Copie a pasta `flyway` do regime regular, gere o banco e suba a API em outro terminal:

```powershell
cd "$env:USERPROFILE\Desktop\calculadora-reforma-tributaria\calculadora-rtc-simples-nacional"
Copy-Item -Recurse -Force ..\calculadora-rtc-regime-regular\flyway .\flyway
New-Item -ItemType Directory -Force calculadora\db | Out-Null
.\mvnw.cmd flyway:migrate "-Dflyway.configFiles=flyway\flyway-pro.conf" --batch-mode --errors
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=offline"
```

A API fica em http://localhost:8082/api e o Swagger em http://localhost:8082/api/swagger-ui.html.

Nesta base, o catálogo de atividades vale a partir de 2027 e o cálculo aceita NF-e (55) e NFC-e (65). As alíquotas efetivas de IBS e CBS são informadas na tela. A API não busca alíquota de referência.

## 7. Tela, porta 8090

Com as duas APIs no ar, em um terceiro terminal:

```powershell
cd "$env:USERPROFILE\Desktop\calculadora-reforma-tributaria\tela-calculadora"
.\mvnw.cmd spring-boot:run
```

- Regime regular: http://localhost:8090/
- Simples Nacional: http://localhost:8090/simples

Os clientes ficam em `tela-calculadora/data/clientes.json`.

## JAR, se quiser rodar sem o Maven

Gere o pacote dentro de cada pasta e execute o JAR a partir da raiz daquele projeto, para o banco em `calculadora\db` ser encontrado. O `java` desse comando também precisa ser o do `JAVA_HOME`.

```powershell
.\mvnw.cmd clean package -DskipTests
java "-Dspring.profiles.active=offline" -jar target\api-regime-geral.jar
```

No Simples Nacional o arquivo é `target\api-simples-nacional.jar`. Na tela, o perfil offline não entra no comando:

```powershell
java -jar target\tela-calculadora-0.1.0.jar
```
