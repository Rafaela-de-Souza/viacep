# Case CEP

API Spring Boot para consulta de endereços via CEP utilizando ViaCEP, com opção de exportação dos resultados para Excel.

## Sumário
- [Arquitetura](#arquitetura)
- [Dependências](#dependências)
- [Endpoints](#endpoints)
- [Como rodar](#como-rodar)
- [Testes](#testes)
- [Exportação para Excel](#exportação-para-excel)

---

## Arquitetura
- **Spring Boot**
- **MVC (Model-View-Controller)**
  - Controller: Recebe requisições HTTP
  - Service: Lógica de negócio e integração com ViaCEP
  - Model: Estrutura dos dados (`CepResponse`, `ConsultaCep`)

Estrutura de pastas:
```
src/main/java/com/case_cep/
 ├─ controller/
 ├─ service/
 ├─ model/
 └─ CaseCepApplication.java
```

---

## Dependências

Incluídas no `pom.xml`:
- `spring-boot-starter-web` (API REST)
- `spring-boot-starter-webflux` (WebClient para integração ViaCEP)
- `spring-boot-starter-test` (Testes unitários)
- `poi-ooxml` (Apache POI para exportação Excel)

---

## Endpoints

- `GET /cep/{cep}`
  - Consulta único CEP (retorna JSON)
- `GET /cep/lista?ceps=01001000,01037010`
  - Consulta múltiplos CEPs (retorna JSON)
- `GET /cep/excel/lista?ceps=01001000,01037010,01218020`
  - Exporta múltiplos CEPs para Excel (`.xlsx`)
- `GET /cep/consultas/excel`
  - Exporta consultas mockadas para Excel

---

## Como rodar

1. Compile o projeto:
   ```
   .\mvnw.cmd clean install
   ```
2. Rode a aplicação:
   ```
   .\mvnw.cmd spring-boot:run
   ```
3. Teste os endpoints no Postman ou navegador.

---

## Testes

Para rodar os testes unitários:
```
.\mvnw.cmd test
```
Testes para os serviços principais e exportação Excel estão em `src/test/java/com/case_cep/service/`.

---

## Exportação para Excel

- O arquivo Excel é gerado com os dados dos CEPs consultados.
- Se nenhum CEP válido for encontrado, o arquivo não é gerado e retorna erro 400.

