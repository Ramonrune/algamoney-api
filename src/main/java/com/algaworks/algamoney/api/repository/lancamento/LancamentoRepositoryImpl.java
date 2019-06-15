package com.algaworks.algamoney.api.repository.lancamento;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.algaworks.algamoney.api.dto.LancamentoEstatisticaCategoria;
import com.algaworks.algamoney.api.model.Lancamento;
import com.algaworks.algamoney.api.repository.filter.LancamentoFilter;
import com.algaworks.algamoney.api.repository.projection.ResumoLancamento;

public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
	public List<LancamentoEstatisticaCategoria> porCategoria(LocalDate mesReferencia) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		
		CriteriaQuery<LancamentoEstatisticaCategoria> criteriaQuery = criteriaBuilder.createQuery(LancamentoEstatisticaCategoria.class);
		
		Root<Lancamento> root =  criteriaQuery.from(Lancamento.class);
		
		
		criteriaQuery.select(criteriaBuilder.construct(
				LancamentoEstatisticaCategoria.class,
				root.get("categoria"),
				criteriaBuilder.sum(root.get("valor"))));
		
		LocalDate primeiroDia = mesReferencia.withDayOfMonth(1);
		LocalDate ultimoDia = mesReferencia.withDayOfMonth(mesReferencia.lengthOfMonth());
		
		
		criteriaQuery.where(
			criteriaBuilder.greaterThanOrEqualTo(root.get("dataVencimento"), primeiroDia),
			criteriaBuilder.lessThanOrEqualTo(root.get("dataVencimento"), ultimoDia)
		);
		
		
		criteriaQuery.groupBy(root.get("categoria"));
		
		
		TypedQuery<LancamentoEstatisticaCategoria> typedQuery = entityManager.createQuery(criteriaQuery);
		
		
		return typedQuery.getResultList();
	}


	
	@Override
	public Page<Lancamento> filtrar(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Lancamento> criteria = builder.createQuery(Lancamento.class);
		
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		//criar as restrições
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		TypedQuery<Lancamento> query = entityManager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}
	
	@Override
	public Page<ResumoLancamento> resumir(LancamentoFilter lancamentoFilter, Pageable pageable) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ResumoLancamento> criteria = builder.createQuery(ResumoLancamento.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		criteria.select(builder.construct(ResumoLancamento.class, 
				root.get("codigo"),
				root.get("descricao"),
				root.get("dataVencimento"),
				root.get("dataPagamento"),
				root.get("valor"),
				root.get("tipo"),
				root.get("categoria").get("nome"),
				root.get("pessoa").get("nome")
				)); 
		
		
		//criar as restrições
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		TypedQuery<ResumoLancamento> query = entityManager.createQuery(criteria);
		adicionarRestricoesDePaginacao(query, pageable);
		
		return new PageImpl<>(query.getResultList(), pageable, total(lancamentoFilter));
	}


	private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder builder,
			Root<Lancamento> root) {
		
		List<Predicate> predicates = new ArrayList<>();
		
		
		//root é tipo:
		//select root from tabela
		
		//where descricao like '%algo%'
		if(!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
			predicates.add(builder.like(
					builder.lower(root.get("descricao")),
					"%" + lancamentoFilter.getDescricao().toLowerCase() + "%"
			));
		}
		
		if(lancamentoFilter.getDataVencimentoDe() != null) {
			predicates.add(builder.greaterThanOrEqualTo(
					root.get("dataVencimento"), 
					lancamentoFilter.getDataVencimentoDe()));

		}
		
		if(lancamentoFilter.getDataVencimentoAte() != null) {
			predicates.add(builder.lessThanOrEqualTo(
					root.get("dataVencimento"),
					lancamentoFilter.getDataVencimentoAte()
					));
			
		} 
		
		return predicates.toArray(new Predicate[predicates.size()]);
	}
	
	private Long total(LancamentoFilter lancamentoFilter) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = builder.createQuery(Long.class);
		Root<Lancamento> root = criteria.from(Lancamento.class);
		
		Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
		criteria.where(predicates);
		
		criteria.select(builder.count(root));
		return entityManager.createQuery(criteria).getSingleResult();
	}

	private void adicionarRestricoesDePaginacao(TypedQuery<?> query, Pageable pageable) {
		int paginaAtual = pageable.getPageNumber();
		int totalRegistrosPorPagina = pageable.getPageSize();
		int primeiroRegistroDaPagina = paginaAtual * totalRegistrosPorPagina;
		query.setFirstResult(primeiroRegistroDaPagina);
		query.setMaxResults(totalRegistrosPorPagina);
		
	}

	
	

	
	
}
