# Gestao Escolar SENAC

Sistema desktop de gestao escolar desenvolvido em Java Swing, com banco de dados SQLite local e telas para diferentes perfis de usuario, como aluno, responsavel, professor, secretaria, funcionario, direcao e administrador.

## Compatibilidade visual

**Ambiente recomendado: Linux.**

No **Linux**, o projeto funciona perfeitamente e a interface fica organizada como esperado.

Em **Windows** e **macOS**, o sistema pode ate abrir e executar, mas a interface fica visualmente bugada: componentes podem aparecer desalinhados, sobrepostos, fora de posicao ou com espacamentos quebrados. Para apresentacao, testes visuais e uso mais estavel, utilize Linux.

Esse comportamento acontece porque muitas telas usam posicionamento absoluto no Swing, com `setLayout(null)` e `setBounds(...)`. Como cada sistema operacional renderiza fontes, bordas e componentes Swing de formas diferentes, as posicoes podem mudar bastante fora do Linux.

## Tecnologias utilizadas

- Java
- Java Swing
- SQLite
- Eclipse IDE
- JARs locais em `resources/JAR`

## Estrutura do projeto

- `src/`: codigo-fonte Java
- `src/view/`: telas da aplicacao
- `src/controller/`: regras de controle
- `src/dao/`: acesso aos dados
- `src/model/`: classes de modelo
- `src/database/`: conexao e inicializacao do banco
- `database/banco.db`: banco SQLite usado pela aplicacao
- `resources/Images/`: imagens e icones do sistema
- `resources/JAR/`: bibliotecas externas
- `ACESSOS_TESTE.md`: usuarios e senhas para teste

## Como executar

### Pelo Eclipse

1. Abra o Eclipse.
2. Importe este diretorio como um projeto Java existente.
3. Confirme se os JARs em `resources/JAR` estao no Build Path.
4. Execute a classe:

```text
src/view/Login.java
```

A classe principal e:

```text
view.Login
```

### Pelo terminal no Linux

Execute os comandos a partir da raiz do projeto:

```bash
javac -encoding UTF-8 -cp "resources/JAR/*" -d bin $(find src -name "*.java")
java -cp "bin:resources/JAR/*" view.Login
```

Importante: a aplicacao usa caminhos relativos para imagens, JARs e banco de dados. Por isso, execute sempre a partir da pasta raiz do projeto.

## Banco de dados

O projeto ja possui um banco SQLite em:

```text
database/banco.db
```

A conexao esta configurada para usar:

```text
jdbc:sqlite:./database/banco.db
```

## Acessos de teste

Os usuarios de teste estao documentados em:

```text
ACESSOS_TESTE.md
```

A senha padrao informada para os acessos de teste e:

```text
Senha@123
```

## Observacoes importantes

- Use Linux para evitar problemas visuais.
- Windows e macOS nao sao recomendados para avaliacao visual do projeto.
- Nao mova as pastas `resources` ou `database`, pois a aplicacao depende desses caminhos relativos.
- Caso a interface esteja desalinhada em outro sistema operacional, isso e esperado neste projeto.
