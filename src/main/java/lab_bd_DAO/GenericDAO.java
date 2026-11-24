package lab_bd_DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

/**
 * Classe genérica para operações CRUD (Create, Read, Update, Delete)
 * utilizando JPA (Java Persistence API).
 *
 * @param <T> Tipo da entidade que será gerenciada
 */
public class GenericDAO<T> {

    // Gerenciador de entidades JPA - responsável pelas operações de persistência
    protected EntityManager em;

    // Classe da entidade - usada para consultas e operações reflexivas
    private Class<T> classe;

    /**
     * Construtor que recebe o EntityManager e a classe da entidade.
     *
     * @param em EntityManager para gerenciar as operações de banco de dados
     * @param classe Classe da entidade (ex: Produto.class, Cliente.class)
     */
    public GenericDAO(EntityManager em, Class<T> classe){
        this.em = em;
        this.classe = classe;
    }

    /**
     * Persiste (insere) um novo objeto no banco de dados.
     *
     * @param obj Objeto a ser salvo
     */
    public void salvar(T obj) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(obj);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    /**
     * Atualiza um objeto existente no banco de dados.
     *
     * @param obj Objeto a ser atualizado (deve ter o ID preenchido)
     */
    public void atualizar(T obj) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(obj);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    /**
     * Exclui um objeto do banco de dados.
     *
     * @param obj Objeto a ser excluído
     */
    public void excluir(T obj) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.remove(em.contains(obj) ? obj : em.merge(obj));
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    /**
     * Busca uma entidade pelo seu ID.
     *
     * @param id Identificador da entidade (chave primária)
     * @return Objeto da entidade encontrado ou null se não existir
     */
    public T buscarPorId(Object id) {
        // find: busca diretamente pelo ID na base de dados
        return em.find(classe, id);
    }

    /**
     * Lista todas as entidades do tipo T.
     *
     * @return Lista com todas as entidades encontradas
     */
    public List<T> listar() {
        // JPQL (Java Persistence Query Language)
        // "FROM " + classe.getSimpleName() - consulta que seleciona todos os registros
        // Exemplo: para Cliente, a query seria "FROM Cliente"
        return em.createQuery("FROM " + classe.getSimpleName(), classe).getResultList();
    }
}