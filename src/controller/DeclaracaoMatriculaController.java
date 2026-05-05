//Guilherme

package controller;

import dao.AlunoDAO;
import dao.FuncionarioDAO;
import dao.TurmaDAO;
import database.ConnectionFactory;
import model.Aluno;
import model.DeclaracaoMatricula;
import model.Funcionario;
import model.Turma;
import model.Usuario;
import util.SessaoUsuario;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DeclaracaoMatriculaController {

    public DeclaracaoMatricula montarDeclaracao(int alunoId, int anoLetivo) {
        if (alunoId <= 0) {
            throw new IllegalArgumentException("ID do aluno é inválido.");
        }

        if (anoLetivo < 2000) {
            throw new IllegalArgumentException("Ano letivo está vazio ou é anterior a 2000.");
        }

        Usuario usuarioLogado = SessaoUsuario.getUsuarioLogado();

        if (usuarioLogado == null) {
            throw new IllegalArgumentException("Nenhum usuário logado.");
        }

        if (usuarioLogado.getTipoUsuario() == null
                || !usuarioLogado.getTipoUsuario().name().equalsIgnoreCase("SECRETARIA")) {
            throw new IllegalArgumentException("Apenas usuários da secretaria podem gerar declaração de matrícula.");
        }

        if (usuarioLogado.getFuncionarioId() <= 0) {
            throw new IllegalArgumentException("O usuário logado não está vinculado a um funcionário.");
        }

        try (Connection conn = ConnectionFactory.getConnection()) {
            AlunoDAO alunoDAO = new AlunoDAO(conn);
            TurmaDAO turmaDAO = new TurmaDAO(conn);
            FuncionarioDAO funcionarioDAO = new FuncionarioDAO(conn);

            Aluno aluno = alunoDAO.buscarPorId(alunoId);

            if (aluno == null) {
                throw new IllegalArgumentException("Aluno não encontrado.");
            }

            Turma turma = turmaDAO.buscarPorId(aluno.getIdTurma());

            if (turma == null) {
                throw new IllegalArgumentException("Turma do aluno não encontrada.");
            }

            Funcionario funcionario = funcionarioDAO.buscarPorId(usuarioLogado.getFuncionarioId());

            if (funcionario == null) {
                throw new IllegalArgumentException("Funcionário vinculado ao usuário logado não foi encontrado.");
            }

            DeclaracaoMatricula declaracao = new DeclaracaoMatricula();

            declaracao.setAlunoId(aluno.getIdAluno());
            declaracao.setNomeAluno(aluno.getNome());
            declaracao.setMatricula(aluno.getMatricula());
            declaracao.setCpf(aluno.getCpf());
            declaracao.setTurma(turma.getDescricaoTurma());
            declaracao.setAnoLetivo(anoLetivo);
            declaracao.setDataEmissao(LocalDate.now());
            declaracao.setNomeFuncionario(funcionario.getNome());
            declaracao.setCargoFuncionario(funcionario.getCargo());

            validarDeclaracao(declaracao);

            return declaracao;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao montar declaração de matrícula.", e);
        }
    }

    public String gerarTextoDeclaracao(DeclaracaoMatricula declaracao) {
        validarDeclaracao(declaracao);

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return """
                							DECLARAÇÃO DE MATRÍCULA

                Declaramos para os fins que se fizerem necessários, que o(a) aluno(a) %s,
                portador(a) da matrícula %s, CPF %s, encontra-se regularmente matriculado(a)
                na turma %s, no ano letivo de %d nesta instituição de ensino.

                Curitibanos, %s.


                __________________________________
                %s
                %s
                """.formatted(
                declaracao.getNomeAluno(),
                declaracao.getMatricula(),
                declaracao.getCpf(),
                declaracao.getTurma(),
                declaracao.getAnoLetivo(),
                declaracao.getDataEmissao().format(formato),
                declaracao.getNomeFuncionario(),
                declaracao.getCargoFuncionario()
        );
    }

    private void validarDeclaracao(DeclaracaoMatricula declaracao) {
        if (declaracao == null) {
            throw new IllegalArgumentException("Declaração de matrícula não pode ser nula.");
        }

        if (declaracao.getAlunoId() <= 0) {
            throw new IllegalArgumentException("ID do aluno é inválido.");
        }

        if (declaracao.getNomeAluno() == null || declaracao.getNomeAluno().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do aluno é obrigatório.");
        }

        if (declaracao.getMatricula() == null || declaracao.getMatricula().trim().isEmpty()) {
            throw new IllegalArgumentException("Matrícula é obrigatória.");
        }

        if (declaracao.getCpf() == null || declaracao.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório.");
        }

        if (declaracao.getTurma() == null || declaracao.getTurma().trim().isEmpty()) {
            throw new IllegalArgumentException("Turma é obrigatória.");
        }

        if (declaracao.getAnoLetivo() < 2000) {
            throw new IllegalArgumentException("Ano letivo está vazio ou é anterior a 2000.");
        }

        if (declaracao.getDataEmissao() == null) {
            throw new IllegalArgumentException("Data de emissão é obrigatória.");
        }

        if (declaracao.getNomeFuncionario() == null || declaracao.getNomeFuncionario().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do funcionário é obrigatório.");
        }

        if (declaracao.getCargoFuncionario() == null || declaracao.getCargoFuncionario().trim().isEmpty()) {
            throw new IllegalArgumentException("Cargo do funcionário é obrigatório.");
        }
    }
}