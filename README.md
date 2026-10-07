# Gestão Escolar SENAC

Sistema desktop para gestão escolar, desenvolvido em Java com interface Swing e banco de dados SQLite.

## Principais recursos

- Cadastro e gerenciamento de alunos, responsáveis, professores e funcionários.
- Organização de turmas, salas e disciplinas.
- Registro de notas, frequência, chamadas, atividades e ocorrências.
- Consulta de boletins e históricos escolares.
- Emissão de avisos, declarações e relatórios em PDF.
- Controle de acesso por perfil de usuário.

## Requisitos

- Java Development Kit (JDK) 25.
- Eclipse IDE (o projeto já contém a configuração `.project` e `.classpath`).

As bibliotecas necessárias estão em `resources/JAR`, incluindo SQLite JDBC, jBCrypt, JCalendar, Jackson e OpenPDF.

## Como executar

1. Importe a pasta do projeto no Eclipse como **Existing Project into Workspace**.
2. Confirme o uso do JDK 25 nas propriedades do projeto.
3. Execute a classe `src/view/Login.java` como **Java Application**.

O banco SQLite é carregado em `database/banco.db`. Execute o sistema com a pasta do projeto como diretório de trabalho para que esse caminho seja localizado corretamente.

## Acesso inicial

A senha padrão utilizada para novos usuários ou redefinições é:

```text
Senha@123
```

O login também exige o CPF cadastrado. Por segurança, altere a senha padrão após o primeiro acesso.

## Estrutura

```text
src/        Código-fonte Java organizado em controller, dao, database, model, util e view
resources/  Imagens e bibliotecas JAR
database/   Banco de dados SQLite
bin/        Arquivos compilados gerados pelo Eclipse (não versionados)
```

## Tecnologia

- Java 25
- Java Swing
- SQLite
- JDBC
- jBCrypt
- OpenPDF
