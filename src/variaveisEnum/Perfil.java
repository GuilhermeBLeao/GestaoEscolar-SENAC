package variaveisEnum;

import java.util.Set;

public enum Perfil {

    SECRETARIA(Set.of(
        Permissao.MATRICULAR_ALUNO,
        Permissao.TRANSFERIR_ALUNO,
        Permissao.CADASTRAR_RESPONSAVEL,
        Permissao.GERENCIAR_TURMAS,
        Permissao.CONTROLAR_DOCUMENTOS,
        Permissao.EMITIR_HISTORICO
    )),

    PROFESSOR(Set.of(
        Permissao.LANCAR_NOTAS,
        Permissao.LANCAR_FREQUENCIA,
        Permissao.REGISTRAR_PLANO_AULA,
        Permissao.ACOMPANHAR_DESEMPENHO
    )),

    DIRETOR(Set.of(
        Permissao.GERENCIAR_FUNCIONARIOS,
        Permissao.APROVAR_MATRICULAS,
        Permissao.VISUALIZAR_DASHBOARDS,
        Permissao.DEFINIR_CALENDARIO,
        Permissao.GERENCIAR_COMUNICADOS,
        Permissao.VISUALIZAR_INDICADORES
    ));

    private final Set<Permissao> permissoes;

    Perfil(Set<Permissao> permissoes) {
        this.permissoes = permissoes;
    }

    public Set<Permissao> getPermissoes() {
        return permissoes;
    }
}