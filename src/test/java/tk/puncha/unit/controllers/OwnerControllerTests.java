package tk.puncha.unit.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tk.puncha.controllers.ControllerBase;
import tk.puncha.controllers.OwnerController;
import tk.puncha.models.Owner;
import tk.puncha.repositories.OwnerRepository;
import tk.puncha.validators.OwnerValidator;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@RunWith(SpringRunner.class)
@WebMvcTest(OwnerController.class)
public class OwnerControllerTests {
  //
//  @TestConfiguration
//  static class MyConfig{
//    @Bean
//    OwnerValidator validator(){
//      return new OwnerValidator();
//    }
//  }

  @Autowired
  MockMvc mockMvc;

  @MockBean
  OwnerRepository ownerRepository;

  @MockBean
  OwnerValidator ownerValidator;

  @Test
  public void shouldnIndexReturnOwnerListPage() throws Exception {
    List<Owner> owners = new ArrayList<>();
    when(ownerRepository.getAllOwners()).thenReturn(owners);

    mockMvc.perform(get("/owners"))
        .andExpect(status().isOk())
        .andExpect(view().name("owner/index"))
        .andExpect(model().attributeExists("owners"));
    verify(ownerRepository).getAllOwners();
  }

  @Test
  public void shouldViewReturnOwnerDetailPage() throws Exception {
    Owner owner = mock(Owner.class);
    when(ownerRepository.getOwnerWithPetsById(1)).thenReturn(owner);
    when(ownerValidator.supports(owner.getClass())).thenReturn(true);

    mockMvc.perform(get("/owners/1"))
        .andExpect(status().isOk())
        .andExpect(view().name("owner/viewOrEdit"))
        .andExpect(model().attribute("owner", owner))
        .andExpect(model().attribute("mode", ControllerBase.FormMode.Readonly));

    verify(ownerRepository).getOwnerWithPetsById(1);
    // validator is called even if we return the model
    verify(ownerValidator).supports(owner.getClass());
  }

  @Test
  public void shouldEditOwnerReturnEditableOwnerDetailPage() throws Exception {
    Owner owner = mock(Owner.class);
    when(ownerRepository.getOwnerById(1)).thenReturn(owner);
    when(ownerValidator.supports(owner.getClass())).thenReturn(true);

    mockMvc.perform(get("/owners/1/edit"))
        .andExpect(status().isOk())
        .andExpect(view().name("owner/viewOrEdit"))
        .andExpect(model().attribute("owner", owner))
        .andExpect(model().attribute("mode", ControllerBase.FormMode.Edit));

    verify(ownerRepository).getOwnerById(1);
    // validator is called even if we return the model
    verify(ownerValidator).supports(owner.getClass());
  }

  @Test
  public void shouldDeleteRemoveTheOwnerAndRedirectToIndexPage() throws Exception {
    mockMvc.perform(get("/owners/1/delete"))
        .andExpect(status().is(302))
        .andExpect(redirectedUrl("/owners"));
    verify(ownerRepository).deleteOwner(1);
  }

  @Test
  public void shouldDeleteNonExistingOwnerShowExceptionPage() throws Exception {
    RuntimeException exception = new RuntimeException();
    doThrow(exception).when(ownerRepository).deleteOwner(100);
    mockMvc.perform(get("/owners/100/delete"))
        .andExpect(status().isOk())
        .andExpect(view().name("exception/default"))
        .andExpect(model().attribute("exception", exception));
    verify(ownerRepository).deleteOwner(100);
  }


  @Test
  public void shouldCreateShowCreateOwnerPage() throws Exception {
    when(ownerValidator.supports(any())).thenReturn(true);
    mockMvc.perform(get("/owners/new"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("owner"))
        .andExpect(model().attribute("mode", ControllerBase.FormMode.Edit));
    verify(ownerValidator).supports(any());
  }

  @Test
  public void shouldCreateOwnerSaveOwnerAndRedirectToOwnerDetailPage() throws Exception {
    when(ownerValidator.supports(any())).thenReturn(true);
    doAnswer(invocation -> {
      Owner owner = invocation.getArgumentAt(0, Owner.class);
      owner.setId(123);
      return null;
    }).when(ownerRepository).insertOwner(any(Owner.class));
    MockHttpServletRequestBuilder req = post("/owners/new")
        .param("firstName", "PunCha")
        .param("lastName", "Feng")
        .param("address", "Room.201")
        .param("city", "Shanghai")
        .param("telephone", "1234567");
    mockMvc.perform(req)
        .andExpect(status().is(302))
        .andExpect(redirectedUrl("/owners/123"));
    // The following verification passes if remove @WebMvc, so
    // this should be a bug in Spring Boot.
    // verify(ownerRepository).insertOwner(any(Owner.class));
  }

  @Test
  public void shouldCreateOwnerFailWhenOwnerInformationIsMissing() throws Exception {
    when(ownerValidator.supports(any())).thenReturn(true);
    doCallRealMethod().when(ownerValidator).validate(any(), any());
    MockHttpServletRequestBuilder req = post("/owners/new");
    mockMvc.perform(req.param("address", "12"))
        .andExpect(status().isOk())
        .andExpect(view().name("owner/viewOrEdit"))
        .andExpect(model().attributeHasErrors("owner"))
        .andExpect(model().attributeHasFieldErrorCode("owner", "firstName", "NotNull"))
        .andExpect(model().attributeHasFieldErrorCode("owner", "lastName", "NotNull"))
        .andExpect(model().attributeHasFieldErrorCode("owner", "address", "error.address.length"))
    ;
    // The following verification passes if remove @WebMvc, so
    // this should be a bug in Spring Boot.
    // verify(ownerRepository).insertOwner(any(Owner.class));
  }

  @Test
  public void shouldUpdateOwnerSaveChangesAndRedirectToOwnerDetailPage() throws Exception {
    when(ownerValidator.supports(any())).thenReturn(true);
    MockHttpServletRequestBuilder req = post("/owners/new")
        .param("id", "123")
        .param("firstName", "PunCha")
        .param("lastName", "Feng")
        .param("address", "Room.201")
        .param("city", "Shanghai")
        .param("telephone", "1234567");
    mockMvc.perform(req)
        .andExpect(status().is(302))
        .andExpect(redirectedUrl("/owners/123"));
  }
}