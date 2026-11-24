# Sistema de Gestão de Frota e Locadora de Veículos

Sistema de backend desenvolvido em Java com persistência relacional para gerenciamento operacional de frotas e controle de viagens, manutenções preventivas, veículos e motoristas.

---

## 1. Objetivo do Sistema

O sistema controla o ciclo de vida dos veículos da frota:
- **Cadastro e controle:** Veículos (placa, modelo, ano, odômetro, status, tipo) e Motoristas (CPF, nome, categoria da CNH).
- **Operação de Viagens:** Registro de início de viagem, alocação de motorista/veículo e encerramento com atualização do odômetro.
- **Manutenção Preventiva:** Registro de tipos de manutenção por quilometragem de referência e controle de ordens de serviço pendentes geradas automaticamente.
- **Integridade Operacional:** Validação automática de CNH para caminhões e disparo de alertas de revisão preventiva a cada ciclo de quilometragem.

---

## 2. Tecnologias Utilizadas

- **Linguagem:** Java 25 (OpenJDK 25 LTS)
- **Gerenciador de Dependências & Build:** Apache Maven 3.9+
- **ORM / JPA:** Jakarta Persistence 3.2 com Hibernate ORM 7.4.2.Final
- **Banco de Dados:** MySQL 8.0+ (Driver: MySQL Connector/J 9.7.0)
- **Testes Automatizados:** JUnit 5 (Jupiter 5.12.0) e Maven Surefire Plugin

---

## 3. Arquitetura e Organização de Pacotes

A aplicação adota uma arquitetura monolítica em camadas limpas:

```
trabalho_labbd/
├── sql/                              # Scripts SQL de schema e rotinas
│   ├── frota.sql                     # DDL de criação do banco, tabelas e constraints
│   ├── procedure.sql                 # Stored Procedures (ENCERRAR_VIAGEM, REGISTRAR_MANUTENCAO)
│   ├── trigger.sql                   # Triggers de domínio (TRG_VALIDA_CNH, TRG_ALERTA_MANUTENCAO)
│   └── insert.sql                    # Carga inicial de dados para desenvolvimento
├── src/main/java/
│   ├── lab_bd_class/                 # Camada de Modelo / Entidades JPA
│   │   ├── Veiculo.java              # Entidade VEICULO
│   │   ├── Motorista.java            # Entidade MOTORISTA
│   │   ├── Viagem.java               # Entidade VIAGEM
│   │   ├── Manutencao.java           # Entidade MANUTENCAO
│   │   ├── ManutencaoVeiculo.java    # Entidade associativa MANUTENCAO_VEICULO
│   │   └── TesteBackend.java         # Executável de demonstração manual
│   └── lab_bd_DAO/                   # Camada de Acesso a Dados (DAOs)
│       ├── GenericDAO.java           # CRUD genérico seguro com rollback
│       ├── JPAUtil.java              # Fábrica de EntityManagerFactory via variáveis de ambiente
│       ├── VeiculoDAO.java           # Consultas e operações de Veículo
│       ├── MotoristaDAO.java         # Consultas e operações de Motorista
│       ├── ManutencaoDAO.java        # Consultas de Manutenção (suporte a Double)
│       ├── ManutencaoVeiculoDAO.java # Consultas de histórico de manutenção
│       ├── ViagemDAO.java            # Operações e integração com Stored Procedure
│       └── TesteDAO.java             # Executável de teste manual dos DAOs
├── src/main/resources/
│   └── META-INF/
│       └── persistence.xml           # Configuração da PU "LabBD" (modo validate)
├── src/test/java/
│   └── lab_bd_DAO/
│       └── DAOTest.java              # Testes automatizados JUnit 5
└── src/test/resources/
    └── META-INF/
        └── persistence.xml           # Configuração da PU "LabBDTest" isolada
```

---

## 4. Modelo de Dados

O banco relacional é composto por 5 tabelas principais:
1. `VEICULO` (`PLACA` [PK], `MODELO`, `ANO`, `QUILOMETRAGEM_ATUAL` [DOUBLE], `STATUS_VEICULO`, `TIPO`).
2. `MOTORISTA` (`CPF` [PK], `NOME`, `CATEGORIA_CNH`).
3. `VIAGEM` (`ID` [PK Auto-Inc], `DATA_SAIDA`, `DATA_CHEGADA`, `KM_INICIAL`, `KM_FINAL`, `PLACA_VEICULO` [FK], `CPF_MOTORISTA` [FK], `STATUS`, `OBSERVACAO`).
4. `MANUTENCAO` (`ID` [PK Auto-Inc], `DESCRICAO`, `KM_REFERENCIA` [DOUBLE]).
5. `MANUTENCAO_VEICULO` (`ID` [PK Auto-Inc], `PLACA_VEICULO` [FK], `ID_MANUTENCAO` [FK], `DATA_GERACAO`, `DATA_MANUTENCAO`, `KM_FEITO`, `STATUS`, `OBSERVACAO`).

---

## 5. Configuração do MySQL e Criação do Banco

Execute os scripts contidos na pasta `sql/` na ordem:

1. `sql/frota.sql`: Cria o banco `frota` e as tabelas com constraints.
2. `sql/procedure.sql`: Cria as stored procedures `ENCERRAR_VIAGEM` e `REGISTRAR_MANUTENCAO`.
3. `sql/trigger.sql`: Cria as triggers de integridade (`TRG_VALIDA_CNH` e `TRG_ALERTA_MANUTENCAO`).
4. *(Opcional)* `sql/insert.sql`: Popula dados de exemplo.

**Atenção sobre os testes automatizados:** a configuração JPA dos testes utiliza um banco separado chamado `frota_test`, evitando interferência nos dados do banco principal.

Os scripts SQL fornecidos foram preparados originalmente para o banco `frota` e alguns selecionam esse banco explicitamente. Portanto, não os execute diretamente esperando que inicializem automaticamente o `frota_test`. Para executar os testes em outro ambiente, prepare previamente nesse banco uma estrutura equivalente, incluindo as tabelas, procedures, triggers e os dados necessários.


---

## 6. Configuração das Variáveis de Ambiente

Para segurança, credenciais de banco **não** são versionadas no `persistence.xml`. Configure no sistema operacional ou sessão do terminal:

| Variável | Descrição | Padrão se omitido |
|---|---|---|
| `DB_USER` | Usuário de conexão ao MySQL | *(obrigatório)* |
| `DB_PASSWORD` | Senha de conexão ao MySQL | *(obrigatório)* |
| `DB_URL` | URL JDBC de conexão | `jdbc:mysql://localhost:3306/frota` |

### Exemplos de configuração no Windows:

**No PowerShell (sessão atual):**
```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = "sua_senha"
```

**No Prompt de Comando (CMD):**
```cmd
set DB_USER=root
set DB_PASSWORD=sua_senha
```

---

## 7. Compilação, Execução e Testes

### Compilar o projeto:
```bash
mvn compile
```

### Executar a suíte de testes automatizados (JUnit 5):
```bash
mvn test
```

### Gerar o pacote JAR:
```bash
mvn package
```

### Executar as classes de demonstração:
```bash
mvn exec:java -Dexec.mainClass="lab_bd_class.TesteBackend"
mvn exec:java -Dexec.mainClass="lab_bd_DAO.TesteDAO"
```

---

## 8. Funcionamento da Stored Procedure `ENCERRAR_VIAGEM`

A rotina de encerramento de viagens está centralizada no banco através da procedure `ENCERRAR_VIAGEM`:
- **Parâmetros:**
  - `P_ID_VIAGEM` (`INT`): Identificador da viagem aberta.
  - `P_KM_FINAL` (`DOUBLE`): Quilometragem registrada no final do percurso.
  - `P_OBSERVACAO` (`VARCHAR(255)`): Observações adicionais do fechamento.
- **Regras Executadas:**
  1. Verifica se a viagem existe. Se não existir, emite erro `VIAGEM NAO ENCONTRADA`.
  2. Valida se `P_KM_FINAL >= KM_INICIAL`. Se for menor, emite erro `KM FINAL NAO PODE SER MENOR QUE O KM INICIAL`.
  3. Atualiza `VIAGEM`: preenche `KM_FINAL`, define `DATA_CHEGADA = NOW()`, altera `STATUS = 'FINALIZADA'` e atualiza `OBSERVACAO`.
  4. Atualiza `VEICULO`: atualiza `QUILOMETRAGEM_ATUAL = P_KM_FINAL` e altera `STATUS_VEICULO = 'DISPONIVEL'`.
  5. Aciona o gatilho `TRG_ALERTA_MANUTENCAO`, que gera ordem preventiva caso o odômetro atinja múltiplo de 10.000 km.
- **Controle Transacional:**
  - A Stored Procedure executa sob a transação aberta pelo Java (`EntityTransaction`).
  - Se qualquer validação falhar, o Java executa `rollback()` e reverte todas as tabelas atomicamente.
  - O método `ViagemDAO.encerrarViagem(...)` atualiza o cache do JPA com `em.refresh()` logo após o commit.
