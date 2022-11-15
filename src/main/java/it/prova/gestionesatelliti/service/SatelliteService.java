package it.prova.gestionesatelliti.service;

import java.util.List;

import it.prova.gestionesatelliti.model.Satellite;

public interface SatelliteService {
	
	public List<Satellite> listAllElements();
	
	public Satellite caricaSingoloElemento(Long id);
	
	public void aggiorna(Satellite satellite);
	
	public void inserisciNuovo(Satellite satellite);
	
	public void rimuovi(Long id);
	
	public List<Satellite> findByExample(Satellite example);
	

}
