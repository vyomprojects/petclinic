package tk.puncha.dao.hibernate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tk.puncha.controllers.PetController;
import tk.puncha.dao.OwnerDAO;
import tk.puncha.models.Owner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.hibernate.jpa.QueryHints.HINT_FETCHGRAPH;

@Profile("hibernate")
@Component
public class HibernateOwnerDAO implements OwnerDAO {

  private static final Logger logger = LoggerFactory.getLogger(PetController.class);

  @PersistenceContext
  private EntityManager em;

  @Override
  public List<Owner> getAllOwners() {
    return em.createQuery("select distinct o from Owner o", Owner.class)
        .setHint(HINT_FETCHGRAPH, em.getEntityGraph("graph.Owners.lazyPets"))
        .getResultList();
  }

  @Override
  public Owner getOwnerById(int ownerId) {
    return em.find(Owner.class, ownerId);
  }

  public Owner getOwnerWithPetsById(int ownerId) {
    String query = "select owner from Owner owner left join fetch owner.pets where owner.id = :ownerId";
    return em.createQuery(query, Owner.class)
        .setParameter("ownerId", ownerId)
        .getSingleResult();
  }

  @Override
  public int insertOwner(Owner owner) {
    em.persist(owner);
    return owner.getId();
  }

  @Override
  public void updateOwner(Owner owner) {
    em.merge(owner);
  }

  @Override
  public void deleteOwner(int id) {
    Owner reference = em.getReference(Owner.class, id);
    logger.error("reference got!");
    em.remove(reference);
    logger.error("remove called!");
  }
}
