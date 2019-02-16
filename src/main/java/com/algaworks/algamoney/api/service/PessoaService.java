package com.algaworks.algamoney.api.service;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.algaworks.algamoney.api.model.Pessoa;
import com.algaworks.algamoney.api.repository.PessoaRepository;

@Service
public class PessoaService {

	@Autowired
	private PessoaRepository pessoaRepository;
	
	public Pessoa atualizar(Long codigo, Pessoa pessoa) {
		Optional<Pessoa> pessoaSalvaOptional = pessoaRepository.findById(codigo);
		if(pessoaSalvaOptional.isEmpty()) {
			throw new EmptyResultDataAccessException(1);
		}
		else{
			Pessoa pessoaSalva = pessoaSalvaOptional.get();
			BeanUtils.copyProperties(pessoa, pessoaSalva, "codigo");
			return pessoaRepository.save(pessoaSalva);
		}
	}
}
