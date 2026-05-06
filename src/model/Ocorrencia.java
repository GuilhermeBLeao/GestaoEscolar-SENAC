//igor

package model;

import java.time.LocalDate;
import variaveisEnum.TipoOcorrencia;

// Modelo de dados para representar uma ocorrência escolar no sistema.
public class Ocorrencia {
    // Identificador único da ocorrência no banco de dados.
    private int idOcorrencia;
    // ID do funcionário ou secretária que registrou a ocorrência.
    private int funcionarioId;
    // ID do aluno relacionado à ocorrência (pode ser 0 quando não houver aluno específico).
    private int alunoId;
    // Tipo de ocorrência registrada (advertência, suspensão, aviso, etc.).
    private TipoOcorrencia tipoOcorrencia;
    // Texto descritivo explicando o que aconteceu na ocorrência.
    private String descricao;
    // Nome da pessoa que fez o registro da ocorrência.
    private String atendenteNome;
    // Data em que a ocorrência foi registrada.
    private LocalDate dataOcorrencia;

    // Retorna o identificador da ocorrência.
    public int getIdOcorrencia() {
        return idOcorrencia;
    }

    // Define o identificador da ocorrência, exigindo valor positivo.
    public void setIdOcorrencia(int idOcorrencia) {
        if (idOcorrencia <= 0) {
            throw new IllegalArgumentException("ID da ocorrência é inválido.");
        }
        this.idOcorrencia = idOcorrencia;
    }

    // Retorna o ID do funcionário que registrou a ocorrência.
    public int getFuncionarioId() {
        return funcionarioId;
    }

    // Define o ID do funcionário/secretária, exigindo valor positivo.
    public void setFuncionarioId(int funcionarioId) {
        if (funcionarioId <= 0) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório.");
        }
        this.funcionarioId = funcionarioId;
    }

    // Retorna o ID do aluno vinculado à ocorrência.
    public int getAlunoId() {
        return alunoId;
    }

    // Define o ID do aluno; o valor pode ser zero quando não houver aluno específico.
    public void setAlunoId(int alunoId) {
        if (alunoId < 0) {
            throw new IllegalArgumentException("ID do aluno é inválido.");
        }
        this.alunoId = alunoId;
    }

    // Retorna o tipo da ocorrência (enum).
    public TipoOcorrencia getTipoOcorrencia() {
        return tipoOcorrencia;
    }

    // Define o tipo da ocorrência, exigindo um valor não nulo.
    public void setTipoOcorrencia(TipoOcorrencia tipoOcorrencia) {
        if (tipoOcorrencia == null) {
            throw new IllegalArgumentException("Tipo de ocorrência é obrigatório.");
        }
        this.tipoOcorrencia = tipoOcorrencia;
    }

    // Retorna a descrição da ocorrência.
    public String getDescricao() {
        return descricao;
    }

    // Define a descrição da ocorrência, removendo espaços em branco.
    public void setDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição da ocorrência é obrigatória.");
        }
        this.descricao = descricao.trim();
    }

    // Retorna o nome do atendente que registrou a ocorrência.
    public String getAtendenteNome() {
        return atendenteNome;
    }

    // Define o nome do atendente, removendo espaços em branco.
    public void setAtendenteNome(String atendenteNome) {
        if (atendenteNome == null || atendenteNome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do atendente é obrigatório.");
        }
        this.atendenteNome = atendenteNome.trim();
    }

    // Retorna a data em que a ocorrência foi registrada.
    public LocalDate getDataOcorrencia() {
        return dataOcorrencia;
    }

    // Define a data da ocorrência, exigindo valor não nulo e não futuro.
    public void setDataOcorrencia(LocalDate dataOcorrencia) {
        if (dataOcorrencia == null) {
            throw new IllegalArgumentException("Data da ocorrência é obrigatória.");
        }
        if (dataOcorrencia.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data da ocorrência não pode ser futura.");
        }
        this.dataOcorrencia = dataOcorrencia;
    }
}
