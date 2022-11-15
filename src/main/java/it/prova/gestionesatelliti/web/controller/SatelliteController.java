package it.prova.gestionesatelliti.web.controller;

import java.util.Date;
import java.util.List;

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

		if (satellite.getDataLancio() != null && satellite.getDataRientro() != null) {
			if (satellite.getDataLancio().after(satellite.getDataRientro())) {
				result.rejectValue(null, null);
				return "satellite/insert";
			}

		}

		if (satellite.getDataLancio() == null && satellite.getDataRientro() != null) {
			result.rejectValue(null, null);
			return "satellite/insert";
		}

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

}
