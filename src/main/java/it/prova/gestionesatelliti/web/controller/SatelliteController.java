package it.prova.gestionesatelliti.web.controller;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;

import it.prova.gestionesatelliti.model.Satellite;
import it.prova.gestionesatelliti.model.StatoSatellite;
import it.prova.gestionesatelliti.service.SatelliteService;

@Controller
@RequestMapping(value = "/satellite")
public class SatelliteController {

	@Autowired
	private SatelliteService satelliteService;

	@GetMapping
	public ModelAndView listAll() {
		ModelAndView mv = new ModelAndView();
		List<Satellite> results = satelliteService.listAllElements();
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}

	@GetMapping("/search")
	public String search() {
		return "satellite/search";

	}

	@PostMapping("/list")
	public String listByExample(Satellite example, ModelMap model) {
		List<Satellite> result = satelliteService.findByExample(example);
		model.addAttribute("satellite_list_attribute", result);
		return "satellite/list";
	}

	@GetMapping("/insert")
	public String create(Model model) {
		model.addAttribute("insert_satellite_attr", new Satellite());
		return "satellite/insert";
	}

	@PostMapping("/save")
	public String save(@Valid @ModelAttribute("insert_satellite_attr") Satellite satellite, BindingResult result,
			RedirectAttributes redirectAttrs) {

		if (result.hasErrors())
			return "satellite/insert";
		
		// DataRientro Validata ma non DataLancio
		if (satellite.getDataLancio() == null && satellite.getDataRientro() != null) {
			result.rejectValue("dataLancio", "satellite.error.dataLancio.invalid");
			return "satellite/insert";
		}
		
		// DataLancio > DataRientro
		if (satellite.getDataRientro() != null && satellite.getDataRientro() != null && satellite.getDataLancio().after(satellite.getDataRientro())) {
			result.rejectValue("dataLancio", "satellite.error.dataLancio.dataRientro.invalid");
			result.rejectValue("dataRientro", "satellite.error.dataLancio.dataRientro.invalid");
			return "satellite/insert";
		}
		
		// DataLancio Futuro il suo stato può essere solo NULL o DISATTIVATO
		if (satellite.getDataLancio() != null && satellite.getDataLancio().after(new Date()) && (satellite.getStato().equals((StatoSatellite.IN_MOVIMENTO))
				|| satellite.getStato().equals((StatoSatellite.FISSO)))) {
			result.rejectValue("stato", "satellite.error.stato.invalid");
			return "satellite/insert";
		}
		
		// Un Satellite Vecchio
		if (satellite.getDataLancio() != null && satellite.getDataRientro() != null && 
				satellite.getDataLancio().before(new Date()) && 
				satellite.getDataRientro().before(new Date()) && !satellite.getStato().equals(null) &&
				!(satellite.getStato().equals((StatoSatellite.DISATTIVATO)))) {
			result.rejectValue("stato", "satellite.error.stato.invalid");
			return "satellite/insert";
		}

		//Validazione Superata
		satelliteService.inserisciNuovo(satellite);
		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}

	@GetMapping("/show/{idSatellite}")
	public String show(@PathVariable(required = true) Long idSatellite, Model model) {
		model.addAttribute("show_satellite_attr", satelliteService.caricaSingoloElemento(idSatellite));
		return "satellite/show";
	}

	@GetMapping("/remove/{idSatellite}")
	public String prepareDelete(@PathVariable(required = true) Long idSatellite, Model model) {
		model.addAttribute("delete_satellite_attr", satelliteService.caricaSingoloElemento(idSatellite));
		return "satellite/delete";
	}

	@PostMapping("/delete")
	public String delete(@RequestParam(required = true) Long idSatellite, RedirectAttributes redirectAttrs) {
		
		Satellite satelliteInstance = satelliteService.caricaSingoloElemento(idSatellite);
		
		if(satelliteInstance.getStato().equals(StatoSatellite.FISSO) || satelliteInstance.getStato().equals(StatoSatellite.IN_MOVIMENTO)) {
			redirectAttrs.addFlashAttribute("errorMessage", "Non puoi cancellare un satelline IN MOVIMENTO o FISSO");
			return "redirect:/satellite";
		}
		
		satelliteService.rimuovi(idSatellite);
		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}

	@GetMapping("/edit/{idSatellite}")
	public String prepareUpdate(@PathVariable(required = true) Long idSatellite, Model model) {
		model.addAttribute("update_satellite_attr", satelliteService.caricaSingoloElemento(idSatellite));
		return "satellite/update";
	}

	@PostMapping("/update")
	public String update(@Valid @ModelAttribute("update_satellite_attr") Satellite satellite, BindingResult result,
			RedirectAttributes redirectAttrs) {

		if (result.hasErrors())
			return "satellite/edit";
		
		satelliteService.aggiorna(satellite);

		redirectAttrs.addFlashAttribute("successMessage", "Operazione eseguita correttamente");
		return "redirect:/satellite";
	}

	// Lancio e Rientro
	@GetMapping("/launch/{idSatellite}")
	public String launch(@PathVariable(required = true) Long idSatellite, Model model,
			RedirectAttributes redirectAttrs) {
		Satellite satellite = satelliteService.caricaSingoloElemento(idSatellite);
		satellite.setStato(StatoSatellite.IN_MOVIMENTO);
		satellite.setDataLancio(new Date());
		satelliteService.aggiorna(satellite);
		redirectAttrs.addFlashAttribute("successMessage", "Satellite Lanciato con Successo");
		return "redirect:/satellite";
	}

	@GetMapping("/reentry/{idSatellite}")
	public String reentry(@PathVariable(required = true) Long idSatellite, Model model,
			RedirectAttributes redirectAttrs) {
		Satellite satellite = satelliteService.caricaSingoloElemento(idSatellite);
		satellite.setStato(StatoSatellite.DISATTIVATO);
		satellite.setDataRientro(new Date());
		satelliteService.aggiorna(satellite);
		redirectAttrs.addFlashAttribute("successMessage", "Satellite Rientrato Con Successo");
		return "redirect:/satellite";
	}

	// Query 1
	@GetMapping("/launchMoreThanTwoYears")
	public ModelAndView launchMoreThanTwoYears() {
		ModelAndView mv = new ModelAndView();
		List<Satellite> results = satelliteService.listAllLaunchMoreThanTwoYears();
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}

	// Query 2
	@GetMapping("/deactivatedButNotReEntered")
	public ModelAndView deactivatedButNotReEntered() {
		ModelAndView mv = new ModelAndView();
		List<Satellite> results = satelliteService.listAllDeactivatedButNotReEntered();
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}

	// Query3
	@GetMapping("/inOrbitButFixed")
	public ModelAndView inOrbitButFixed() {
		ModelAndView mv = new ModelAndView();
		List<Satellite> results = satelliteService.listAllinOrbitButFixed();
		mv.addObject("satellite_list_attribute", results);
		mv.setViewName("satellite/list");
		return mv;
	}
	
	//Swiftch off switchoff
	@GetMapping("/switchoff")
	public ModelAndView switchoff() {
		ModelAndView mv = new ModelAndView();
		List<Satellite> results = satelliteService.listAllElements();
		Integer risultatiTotali = results.size();
				
		results = results.stream().filter
				(s -> s.getStato().equals(StatoSatellite.FISSO)||
						s.getStato().equals(StatoSatellite.IN_MOVIMENTO)
						&&(s.getDataRientro()==null || s.getDataRientro().after(new Date()))
						).collect(Collectors.toList());
		
		Integer risultatiDaModificare = results.size();
		
		mv.addObject("satellite_list_attribute", results);
		mv.addObject("satellite_count_all_attribute", risultatiTotali);
		mv.addObject("satellite_count_modify_attribute", risultatiDaModificare);
		mv.setViewName("satellite/disable");
		return mv;
	}
	
	@PostMapping("/disable")
	public ModelAndView disable() {
		ModelAndView mv = new ModelAndView();
		
		List<Satellite> results = satelliteService.listAllElements();
		Integer risultatiTotali = results.size();
				
		results = results.stream().filter
				(s -> s.getStato().equals(StatoSatellite.FISSO)||
						s.getStato().equals(StatoSatellite.IN_MOVIMENTO)
						&&(s.getDataRientro()==null || s.getDataRientro().after(new Date()))
						).collect(Collectors.toList());
		
		for (Satellite satellite : results) {
			satellite.setDataRientro(new Date());
			satellite.setStato(StatoSatellite.DISATTIVATO);
			satelliteService.aggiorna(satellite);
		}
			
		mv.addObject("satellite_list_attribute", null);
		mv.addObject("satellite_count_all_attribute", risultatiTotali);
		mv.addObject("satellite_count_modify_attribute", 0);
		mv.setViewName("satellite/disable");
		return mv;
	}

}
