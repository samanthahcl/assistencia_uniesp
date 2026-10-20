package br.edu.uniesp.assistencia.internal.ordemservico.repository;

import br.edu.uniesp.assistencia.internal.ordemservico.entity.OrdemServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServicoEntity, Long> {
}
