package it.prova.gestionesatelliti.repository;


import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import it.prova.gestionesatelliti.model.Satellite;
import it.prova.gestionesatelliti.model.StatoSatellite;

public interface SatelliteRepository extends CrudRepository<Satellite, Long>, JpaSpecificationExecutor<Satellite> {

	List<Satellite> findByDataLancioBefore(Date tenYearsAgoDate);

	//List<Satellite> findByStato(StatoSatellite stato);

	List<Satellite> findByStatoAndDataLancio(StatoSatellite stato, Object object);

	List<Satellite> findByStatoAndDataRientroLikeAndDataLancioBefore(StatoSatellite fisso, Object object,
			Date tenYearsAgoDate);

	List<Satellite> findByStatoAndDataLancioBefore(StatoSatellite fisso, Date tenYearsAgoDate);

}
