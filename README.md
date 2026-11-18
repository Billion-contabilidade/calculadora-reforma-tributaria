# Calculadora da Reforma Tributária

Tela do escritório para consultar as calculadoras oficiais da Receita Federal da reforma tributária do consumo. São duas contas diferentes, cada uma com a própria API:

| Cálculo | Quem usa | API | Tela |
|---|---|---|---|
| Regime regular (Lucro Presumido e Lucro Real) | CBS, IBS e Imposto Seletivo a partir de NCM ou NBS | http://localhost:8080/api | http://localhost:8090/ |
| Simples Nacional | CBS e IBS com as alíquotas efetivas do cliente | http://localhost:8082/api | http://localhost:8090/simples |

A tela fica em `tela-calculadora`. Ela não sobe as APIs: as três aplicações precisam estar rodando ao mesmo tempo, cada uma no seu terminal.

As calculadoras oficiais são estes repositórios:

- https://github.com/ReceitaFederal/calculadora-rtc-regime-regular
- https://github.com/ReceitaFederal/calculadora-rtc-simples-nacional

## O que instalar

- JDK 21 ou superior. O [Temurin 21](https://adoptium.net/) serve.
- Internet na primeira execução. O Maven Wrapper (`mvnw.cmd`) baixa o Maven sozinho.

O wrapper usa o Java de `JAVA_HOME`, não o primeiro `java` do PATH. No PowerShell, aponte para a pasta do JDK 21 antes de cada comando:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
java -version
```

Troque o caminho se o JDK estiver em outra pasta. A versão impressa precisa ser 21. Rode o `mvnw.cmd` de dentro da pasta de cada projeto.

## Clonar

```powershell
git clone https://github.com/Billion-contabilidade/calculadora-reforma-tributaria.git
cd calculadora-reforma-tributaria
git clone https://github.com/ReceitaFederal/calculadora-rtc-regime-regular.git
git clone https://github.com/ReceitaFederal/calculadora-rtc-simples-nacional.git
```

Se essas duas pastas já existirem com o código da Receita, não clone de novo.

## 1. Regime regular, porta 8080

O perfil `offline` só inicia se o banco SQLite existir. Gere o banco uma vez, a partir de `calculadora-rtc-regime-regular`:

```powershell
cd calculadora-rtc-regime-regular
New-Item -ItemType Directory -Force calculadora\db | Out-Null
.\mvnw.cmd flyway:migrate "-Dflyway.configFiles=flyway\flyway-pro.conf" --batch-mode --errors
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=offline"
```

A API fica em http://localhost:8080/api e o Swagger em http://localhost:8080/api/swagger-ui.html.

Deixe esse terminal aberto. Em 2026 a API lê as alíquotas do banco. A partir de 2027 ela exige alíquotas nominais e o resultado é simulado.

## 2. Simples Nacional, porta 8082

Os scripts SQL não vêm no repositório do Simples Nacional. Copie a pasta `flyway` do regime regular, gere o banco e suba a API em outro terminal:

```powershell
cd calculadora-rtc-simples-nacional
Copy-Item -Recurse -Force ..\calculadora-rtc-regime-regular\flyway .\flyway
New-Item -ItemType Directory -Force calculadora\db | Out-Null
.\mvnw.cmd flyway:migrate "-Dflyway.configFiles=flyway\flyway-pro.conf" --batch-mode --errors
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=offline"
```

A API fica em http://localhost:8082/api e o Swagger em http://localhost:8082/api/swagger-ui.html.

Nesta base, o catálogo de atividades vale a partir de 2027 e o cálculo aceita NF-e (55) e NFC-e (65). As alíquotas efetivas de IBS e CBS são informadas na tela. A API não busca alíquota de referência.

## 3. Tela, porta 8090

Com as duas APIs no ar, em um terceiro terminal:

```powershell
cd tela-calculadora
.\mvnw.cmd spring-boot:run
```

- Regime regular: http://localhost:8090/
- Simples Nacional: http://localhost:8090/simples

Os clientes ficam em `tela-calculadora/data/clientes.json`.

## JAR, se quiser rodar sem o Maven

Gere o pacote dentro de cada pasta e execute o JAR a partir da raiz daquele projeto, para o banco em `calculadora\db` ser encontrado.

```powershell
.\mvnw.cmd clean package -DskipTests
java "-Dspring.profiles.active=offline" -jar target\api-regime-geral.jar
```

No Simples Nacional o arquivo é `target\api-simples-nacional.jar`. Na tela, o perfil offline não entra no comando:

```powershell
java -jar target\tela-calculadora-0.1.0.jar
```
