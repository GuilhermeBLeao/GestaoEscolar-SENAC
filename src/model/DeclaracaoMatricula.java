//Guilherme

package model;

import java.time.LocalDate;

public class DeclaracaoMatricula {

    private String nomeAluno, matricula, turma, cpf, cargoFuncionario, nomeFuncionario;
    private LocalDate dataEmissao;
    private int alunoId, anoLetivo;

    public int getAlunoId() { return alunoId; }
    public void setAlunoId(int alunoId) {
        if (alunoId <= 0) {
            throw new IllegalArgumentException("ID do aluno é inválido.");
        }
        this.alunoId = alunoId;
    }

    public int getAnoLetivo() { return anoLetivo; }
    public void setAnoLetivo(int anoLetivo) {
        if (anoLetivo < 2000) {
            throw new IllegalArgumentException("Ano letivo está vazio ou é anterior a 2000.");
        }
        this.anoLetivo = anoLetivo;
    }

    public String getNomeAluno() { return nomeAluno; }
    public void setNomeAluno(String nomeAluno) {
        if (nomeAluno == null || nomeAluno.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo nome do aluno é obrigatório.");
        }
        this.nomeAluno = nomeAluno.trim();
    }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo matrícula é obrigatório.");
        }
        this.matricula = matricula.trim();
    }

    public String getTurma() { return turma; }
    public void setTurma(String turma) {
        if (turma == null || turma.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo turma é obrigatório.");
        }
        this.turma = turma.trim();
    }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo CPF é obrigatório.");
        }
        this.cpf = cpf.trim();
    }

    public String getCargoFuncionario() { return cargoFuncionario; }
    public void setCargoFuncionario(String cargoFuncionario) {
        if (cargoFuncionario == null || cargoFuncionario.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo cargo do funcionário é obrigatório.");
        }
        this.cargoFuncionario = cargoFuncionario.trim();
    }

    public String getNomeFuncionario() { return nomeFuncionario; }
    public void setNomeFuncionario(String nomeFuncionario) {
        if (nomeFuncionario == null || nomeFuncionario.trim().isEmpty()) {
            throw new IllegalArgumentException("Campo nome do funcionário é obrigatório.");
        }
        this.nomeFuncionario = nomeFuncionario.trim();
    }

    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) {
        if (dataEmissao == null) {
            throw new IllegalArgumentException("Campo data de emissão é obrigatório.");
        }
        if (dataEmissao.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de emissão não pode ser futura.");
        }
        this.dataEmissao = dataEmissao;
    }
}