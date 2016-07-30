package tk.puncha.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import tk.puncha.models.Pet;
import tk.puncha.models.Visit;
import tk.puncha.repositories.PetRepository;
import tk.puncha.repositories.VisitRepository;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/pets/{petId}/visits")
public class VisitController {

  private final PetRepository petRepository;
  private final VisitRepository visitRepository;

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.setDisallowedFields("id");
  }

  @Autowired
  public VisitController(PetRepository petRepository, VisitRepository visitRepository) {
    this.petRepository = petRepository;
    this.visitRepository = visitRepository;
  }

  @ModelAttribute("pet")
  Pet getPetById(@PathVariable int petId) {
    return petRepository.getPetById(petId);
  }

  @RequestMapping(path = "{visitId}/delete")
  public String deleteVisit(@PathVariable int visitId) {
    visitRepository.delete(visitId);
    return "redirect:/pets/{petId}";
  }

  @RequestMapping(path = "/new", method = RequestMethod.GET)
  public ModelAndView initCreationForm() {
    return new ModelAndView("visit/new").addObject(new Visit());
  }

  @RequestMapping(path = "/new", method = RequestMethod.POST)
  public String processCreationForm(Pet pet, @ModelAttribute @Valid Visit visit, BindingResult bingResult) {
    if (bingResult.hasErrors()) {
      return "visit/new";
    }

    pet.getVisits().add(visit);
    visit.setPet(pet);
    petRepository.updatePet(pet);
    return "redirect:/pets/{petId}";
  }
}
