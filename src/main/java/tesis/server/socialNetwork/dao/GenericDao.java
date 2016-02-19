package tesis.server.socialNetwork.dao;


import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.ejb.HibernateEntityManager;
import org.json.JSONObject;

import tesis.server.socialNetwork.entity.PostEntity;


/**
 * Clase que implementa las operaciones CRUD y de listado b�sicas
 *
 * @author eferreira
 *
 * @param <T> tipo del objeto a administrar
 * @param <ID> tipo del atributo ID del objeto a administrar
 */
public abstract class GenericDao<T, ID extends Serializable> {

    @PersistenceContext(unitName="RestServer-unit")
    protected EntityManager em;

    public GenericDao() {
    }

    protected abstract Class<T> getEntityBeanType();

    private EntityManager getEm() {
        if (em == null) {
            throw new IllegalStateException("EntityManager no esta seteado");
        }
        return em;
    }

    protected SessionFactory getSessionFactory() {
        if (this.em.getDelegate() instanceof HibernateEntityManager) {
            return ((HibernateEntityManager) this.getEm().getDelegate()).getSession().getSessionFactory();
        } else {
            return ((Session) this.getEm().getDelegate()).getSessionFactory();
        }
    }
    
    protected Session getSession() {
        if (this.em.getDelegate() instanceof HibernateEntityManager) {
            return ((HibernateEntityManager) this.getEm().getDelegate()).getSession();
        } else {
            return ((Session) this.getEm().getDelegate());
        }
    }

    @SuppressWarnings("unchecked")
	public List<T> getListOfEntitiesWithRestrictionsLike(Class<T> clase, JSONObject jsonRestrictions){
    	Criteria criteria = getSession().createCriteria(clase);
    	Iterator<String> keys = jsonRestrictions.keys();
    	while (keys.hasNext()) {
			String entityAttribute = keys.next();
			Object attributeValue = jsonRestrictions.get(entityAttribute);
			//add agrega las restricciones como un AND
			criteria.add(Restrictions.like(entityAttribute, "%"+attributeValue+"%").ignoreCase());
		}
    	
    	return criteria.list();
    }
    
    
    /**
     * permite obtener un objeto de tipo T en funcion a su valor de ID
     * lanza un EntityNotFoundException en caso de que no se encuentre la entidad
     * @param id
     * @return
     */
    public T getById(ID id) {
        T entity = getEm().getReference(getEntityBeanType(), id);
        getEm().lock(entity, LockModeType.WRITE);
        return entity;
    }
    
    
    /**
     * Metodo que retorna un objeto de tipo T en funcion de su clase y de su primaryKey
     * Retorna NULL en caso de que no lo encuentre
     * @param clase
     * @param id
     * @return
     */
    public T findByClassAndID(Class<T> clase, ID id){
    	T entity = getEm().find(clase, id);
    	//TODO verificar la concurrencua, lanza error cuando el objeto es bloqueado. VERIFICAR
    	//getEm().lock(entity, LockModeType.WRITE);
    	return entity;
    }



    /**
     * metodo que permite guardar un objeto de tipo T
     * @param entity
     */
    protected void save(T entity) {
        this.getEm().persist(entity);
    }
    
    /**
     * Metodo que permite guardar un post y retornar su ID generado
     * @param entity
     * @return
     */
    protected Integer saveAndReturnPost(PostEntity entity){
    	this.getEm().persist(entity);
    	this.getEm().flush();
    	return entity.getIdPost();
    }

    /**
     * m�todo para actualizar un objeto de tipo T
     * @param entity
     */
    protected void update(T entity) {
        this.getSession().update(entity);
    }

    /**
     * metodo para eliminar un objeto de tipo T en funcion a su ID
     * @param id
     * @throws Exception
     */
    public void delete(ID id) {
        T entity = this.getById(id);
        if (entity != null) {
            this.delete(entity);
        }
    }

    /**
     * Metodo para eliminar un objeto de tipo T
     * @param entity
     * @throws Exception
     */
    public void delete(T entity) {
        this.getEm().remove(entity);
    }

}
