package it.prova.gestionesatelliti.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.prova.gestionesatelliti.model.Satellite;
import it.prova.gestionesatelliti.model.StatoSatellite;
import it.prova.gestionesatelliti.repository.SatelliteRepository;


@Service
public class SatelliteServiceImpl implements SatelliteService {
	
	@Autowired
	private SatelliteRepository repository;
	
	@Autowired
	private EntityManager entityManager;

	@Override
	@Transactional(readOnly = true)
	public List<Satellite> listAllElements() {
		// TODO Auto-generated method stub
		return (List<Satellite>) repository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Satellite caricaSingoloElemento(Long id) {
		// TODO Auto-generated method stub
		return repository.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void aggiorna(Satellite satellite) {
		// TODO Auto-generated method stub
		repository.save(satellite);
		
	}

	@Override
	@Transactional
	public void inserisciNuovo(Satellite satellite) {
		// TODO Auto-generated method stub
		repository.save(satellite);
	}

	@Override
	@Transactional
	public void rimuovi(Long id) {
		// TODO Auto-generated method stub
		repository.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Satellite> findByExample(Satellite example) {
		// TODO Auto-generated method stub
		Specification<Satellite> specificationCriteria = (root, query, cb) -> {

			List<Predicate> predicates = new ArrayList<Predicate>();

			if (StringUtils.isNotEmpty(example.getDenominazione()))
				predicates.add(cb.like(cb.upper(root.get("nome")), "%" + example.getDenominazione().toUpperCase() + "%"));

			if (StringUtils.isNotEmpty(example.getCodice()))
				predicates.add(cb.like(cb.upper(root.get("cognome")), "%" + example.getCodice().toUpperCase() + "%"));

			if (example.getDataLancio() != null)
				predicates.add(cb.greaterThanOrEqualTo(root.get("dataLancio"), example.getDataLancio()));
			
			if (example.getDataRientro() != null)
				predicates.add(cb.greaterThanOrEqualTo(root.get("dataRientro"), example.getDataRientro()));
			
			if (example.getStato() != null)
				predicates.add(cb.equal(root.get("stato"), example.getStato()));

			return cb.and(predicates.toArray(new Predicate[predicates.size()]));
		};

		return repository.findAll(specificationCriteria);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Satellite> listAllLaunchMoreThanTwoYears() {
		// TODO Auto-generated method stub
		LocalDate tenYearsAgo = LocalDate.now().minusYears(10);
		Date tenYearsAgoDate = Date.from(tenYearsAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
		return repository.findByDataLancioBefore(tenYearsAgoDate);
		
		
	}

	@Override
	@Transactional(readOnly = true)
	public List<Satellite> listAllDeactivatedButNotReEntered() {
		// TODO Auto-generated method stub
		StatoSatellite stato = StatoSatellite.DISATTIVATO;
		return repository.findByStatoAndDataLancio(stato,null);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Satellite> listAllinOrbitButFixed() {
		// TODO Auto-generated method stub
		StatoSatellite stato = StatoSatellite.FISSO;
		return repository.findByStatoAndDataRientro(stato,null);
	}

	
	

}
