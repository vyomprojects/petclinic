package tk.puncha.restfulControllers;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import tk.puncha.models.Owner;
import tk.puncha.repositories.OwnerRepository;
import tk.puncha.validators.OwnerValidator;
import tk.puncha.views.json.view.OwnerJsonView;

import javax.validation.Valid;
import java.util.List;

@RestController  // Includes @ResponseBody
@RequestMapping(path = "/api/owners", produces = MediaType.APPLICATION_JSON_VALUE)
public class RestfulOwnerController {


  private final OwnerRepository ownerRepository;
  private final OwnerValidator ownerValidator;

  @Autowired
  public RestfulOwnerController(OwnerRepository ownerRepository, OwnerValidator ownerValidator) {
    this.ownerRepository = ownerRepository;
    this.ownerValidator = ownerValidator;
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorInfo handleException(Exception exception) {
    return new ErrorInfo(String.format("Unhandled exception %s", exception.getMessage()));
  }

  @InitBinder("owner")
  public void dataBinding(WebDataBinder binder) {
    binder.addValidators(ownerValidator);
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  @JsonView(OwnerJsonView.Default.class)
  public List<Owner> getAllOwners(@RequestParam(required = false) String firstName) {
    if (firstName != null && !firstName.isEmpty())
      return ownerRepository.getOwnersByFirstName(firstName);
    else
      return ownerRepository.getAllOwners();
  }

  @GetMapping("{ownerId}")
  @JsonView(OwnerJsonView.WithPets.class)
  public Owner getOwner(@PathVariable int ownerId) {
    return ownerRepository.getOwnerWithPetsById(ownerId);
  }

  @DeleteMapping("{ownerId}")
  public void deleteOwner(@PathVariable int ownerId) {
    ownerRepository.deleteOwner(ownerId);
  }

  @PostMapping
  public ResponseEntity createOwner(@Valid @RequestBody Owner owner, BindingResult error) {
    if (error.hasErrors()) {
      ErrorInfo errorInfo = new ErrorInfo(
          String.format("Field: %s is invalid.", error.getFieldError().getField()));
      return ResponseEntity.badRequest().body(errorInfo);
    }
    ownerRepository.insertOwner(owner);
    return ResponseEntity.ok(owner);
  }

  @PostMapping("{ownerId}")
  public ResponseEntity updateOwner(@Valid @RequestBody Owner owner, BindingResult error) {
    if (error.hasErrors()) {
      ErrorInfo errorInfo = new ErrorInfo(
          String.format("Field: %s is invalid.", error.getFieldError().getField()));
      return ResponseEntity.badRequest().body(errorInfo);
    }
    // clear the pet collection to avoid updating pets
    owner.getPets().clear();
    ownerRepository.updateOwner(owner);
    return ResponseEntity.noContent().build();
  }
}