package br.edu.uniesp.assistencia.internal.equipamento.repository;

import br.edu.uniesp.assistencia.internal.equipamento.entity.EquipamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipamentoRepository extends JpaRepository<EquipamentoEntity, Long> {
}
