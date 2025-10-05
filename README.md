# Case CEP

API Spring Boot para consulta de endereços via CEP usando ViaCEP com cache Caffeine e exportação Excel.

## Endpoints

### Consulta de Endereços
- `GET /enderecos?cep=01001000` - Consulta CEP único
- `GET /enderecos?cep=01001000,01037010` - Consulta múltiplos CEPs (até 50)
- `GET /enderecos/excel?cep=01001000,01037010` - Exporta CEPs para Excel

### Gerenciamento de Cache
- `DELETE /api/cache/limpar` - Limpa todo o cache
- `DELETE /api/cache/remover/{cep}` - Remove CEP específico do cache
- `GET /api/cache/stats` - Estatísticas do cache
- `GET /api/cache/exists/{cep}` - Verifica se CEP está em cache

## Arquitetura

**Tecnologias:**
- Spring Boot (Web, Cache)
- Caffeine (Cache em memória)
- Apache POI (Exportação Excel)
- ViaCEP API (Consulta de endereços)

**Estrutura MVC:**
```
src/main/java/com/case_cep/
├── controller/     # EnderecoController, CacheController
├── service/        # EnderecoService, CacheManagementService, etc
├── model/          # EnderecoResponse, ConsultaEndereco, etc
├── config/         # CacheConfig
└── CaseCepApplication.java
```

## Como executar

1. **Executar aplicação:**
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Testar aplicação:**
   ```bash
   curl "http://localhost:8080/enderecos?cep=01001000"
   ```

## Testes

Para executar os testes unitários:
```bash
./mvnw test
```

Testes disponíveis:
- `EnderecoServiceTest` - Testes do serviço de endereços
- `CacheIntegrationTest` - Testes de integração do cache
- `EnderecoValidatorTest` - Testes de validação de CEP
- `ExcelExportServiceTest` - Testes de exportação Excel

