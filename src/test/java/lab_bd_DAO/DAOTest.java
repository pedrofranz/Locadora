package lab_bd_DAO;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lab_bd_class.*;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DAOTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    private MotoristaDAO motoristaDAO;
    private VeiculoDAO veiculoDAO;
    private ManutencaoDAO manutencaoDAO;
    private ViagemDAO viagemDAO;

    @BeforeAll
    public static void setUpClass() {
        emf = JPAUtil.getEntityManagerFactory("LabBDTest");
    }

    @AfterAll
    public static void tearDownClass() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @BeforeEach
    public void setUp() {
        em = emf.createEntityManager();
        motoristaDAO = new MotoristaDAO(em);
        veiculoDAO = new VeiculoDAO(em);
        manutencaoDAO = new ManutencaoDAO(em);
        viagemDAO = new ViagemDAO(em);
    }

    @AfterEach
    public void tearDown() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    // ==========================================
    // 1. CRUD MOTORISTA
    // ==========================================
    @Test
    @Order(1)
    public void testCrudMotorista() {
        String cpf = "99988877766";
        Motorista existente = motoristaDAO.buscarPorCpf(cpf);
        if (existente != null) {
            motoristaDAO.excluir(existente);
        }

        Motorista mot = new Motorista();
        mot.setCpf(cpf);
        mot.setNome("Motorista Teste");
        mot.setCategoriaCnh("D");

        motoristaDAO.salvar(mot);
        assertNotNull(motoristaDAO.buscarPorCpf(cpf));

        mot.setNome("Motorista Teste Atualizado");
        motoristaDAO.atualizar(mot);
        Motorista atualizado = motoristaDAO.buscarPorCpf(cpf);
        assertEquals("Motorista Teste Atualizado", atualizado.getNome());

        List<Motorista> lista = motoristaDAO.listarMotoristas();
        assertTrue(lista.stream().anyMatch(m -> m.getCpf().equals(cpf)));

        motoristaDAO.excluir(atualizado);
        assertNull(motoristaDAO.buscarPorCpf(cpf));
    }

    // ==========================================
    // 2. CRUD VEICULO
    // ==========================================
    @Test
    @Order(2)
    public void testCrudVeiculo() {
        String placa = "TST9999";
        Veiculo existente = veiculoDAO.buscarPorPlaca(placa);
        if (existente != null) {
            veiculoDAO.excluir(existente);
        }

        Veiculo veic = new Veiculo();
        veic.setPlaca(placa);
        veic.setModelo("Carro Teste");
        veic.setAno(2023);
        veic.setQuilometragemAtual(10000.50);
        veic.setStatusVeiculo("DISPONIVEL");
        veic.setTipo("CARRO");

        veiculoDAO.salvar(veic);
        assertNotNull(veiculoDAO.buscarPorPlaca(placa));

        veic.setQuilometragemAtual(10500.75);
        veiculoDAO.atualizar(veic);
        Veiculo atualizado = veiculoDAO.buscarPorPlaca(placa);
        assertEquals(10500.75, atualizado.getQuilometragemAtual());

        List<Veiculo> carros = veiculoDAO.buscarPorTipo("CARRO");
        assertTrue(carros.stream().anyMatch(v -> v.getPlaca().equals(placa)));

        List<Veiculo> disponiveis = veiculoDAO.buscarPorStatus("DISPONIVEL");
        assertTrue(disponiveis.stream().anyMatch(v -> v.getPlaca().equals(placa)));

        veiculoDAO.excluir(atualizado);
        assertNull(veiculoDAO.buscarPorPlaca(placa));
    }

    // ==========================================
    // 3. CRUD MANUTENCAO E BUSCA COM DOUBLE
    // ==========================================
    @Test
    @Order(3)
    public void testCrudManutencaoEBuscaPorKmDouble() {
        String descricao = "Revisao Automatizada Teste";
        List<Manutencao> existentes = manutencaoDAO.buscarPorDescricao(descricao);
        for (Manutencao m : existentes) {
            manutencaoDAO.excluir(m);
        }

        Manutencao man = new Manutencao();
        man.setDescricao(descricao);
        man.setKmReferencia(15000.50);

        manutencaoDAO.salvar(man);
        assertNotNull(man.getId());

        // Busca com Double
        List<Manutencao> comKmMenor = manutencaoDAO.buscarPorKmReferencia(20000.0);
        assertTrue(comKmMenor.stream().anyMatch(m -> m.getId().equals(man.getId())));

        List<Manutencao> comKmMaior = manutencaoDAO.buscarPorKmReferencia(10000.0);
        assertFalse(comKmMaior.stream().anyMatch(m -> m.getId().equals(man.getId())));

        manutencaoDAO.excluir(man);
        assertNull(manutencaoDAO.buscarPorId(man.getId()));
    }

    // ==========================================
    // 4. CRIACAO E FLUXOS DE VIAGEM E PROCEDURE
    // ==========================================
    @Test
    @Order(4)
    public void testCriacaoEEncerramentoValidoViagem() {
        // Prepara veiculo e motorista
        String placa = "VIAG001";
        String cpf = "11122233344";

        Veiculo veic = veiculoDAO.buscarPorPlaca(placa);
        if (veic == null) {
            veic = new Veiculo();
            veic.setPlaca(placa);
            veic.setModelo("Sedan Teste");
            veic.setAno(2022);
            veic.setQuilometragemAtual(20000.00);
            veic.setStatusVeiculo("DISPONIVEL");
            veic.setTipo("CARRO");
            veiculoDAO.salvar(veic);
        }

        Motorista mot = motoristaDAO.buscarPorCpf(cpf);
        if (mot == null) {
            mot = new Motorista();
            mot.setCpf(cpf);
            mot.setNome("Condutor Viagem");
            mot.setCategoriaCnh("B");
            motoristaDAO.salvar(mot);
        }

        // Criar Viagem
        Viagem viagem = new Viagem();
        viagem.setVeiculo(veic);
        viagem.setMotorista(mot);
        viagem.setDataSaida(LocalDateTime.now());
        viagem.setKmInicial(20000.00);
        viagem.setStatus("EM_ANDAMENTO");
        viagemDAO.salvar(viagem);

        Integer idViagem = viagem.getId();
        assertNotNull(idViagem);

        // Encerramento válido com precisão decimal
        Double kmFinal = 20150.75;
        viagemDAO.encerrarViagem(idViagem, kmFinal, "Finalizada com sucesso");

        Viagem encerrada = viagemDAO.buscarPorId(idViagem);
        assertEquals(kmFinal, encerrada.getKmFinal());
        assertNotNull(encerrada.getDataChegada());
        assertEquals("FINALIZADA", encerrada.getStatus());
        assertEquals("Finalizada com sucesso", encerrada.getObservacao());

        Veiculo veiculoAtualizado = veiculoDAO.buscarPorPlaca(placa);
        assertEquals(kmFinal, veiculoAtualizado.getQuilometragemAtual());
        assertEquals("DISPONIVEL", veiculoAtualizado.getStatusVeiculo());
    }

    @Test
    @Order(5)
    public void testRejeicaoKmFinalMenorQueInicial() {
        String placa = "VIAG002";
        String cpf = "55566677788";

        Veiculo veic = veiculoDAO.buscarPorPlaca(placa);
        if (veic == null) {
            veic = new Veiculo();
            veic.setPlaca(placa);
            veic.setModelo("Van Teste");
            veic.setAno(2021);
            veic.setQuilometragemAtual(30000.00);
            veic.setStatusVeiculo("DISPONIVEL");
            veic.setTipo("VAN");
            veiculoDAO.salvar(veic);
        }

        Motorista mot = motoristaDAO.buscarPorCpf(cpf);
        if (mot == null) {
            mot = new Motorista();
            mot.setCpf(cpf);
            mot.setNome("Motorista Van");
            mot.setCategoriaCnh("D");
            motoristaDAO.salvar(mot);
        }

        Viagem viagem = new Viagem();
        viagem.setVeiculo(veic);
        viagem.setMotorista(mot);
        viagem.setDataSaida(LocalDateTime.now());
        viagem.setKmInicial(30000.00);
        viagem.setStatus("EM_ANDAMENTO");
        viagemDAO.salvar(viagem);

        // Tentar encerrar com kmFinal < kmInicial (ex: 29900.0)
        Exception exception = assertThrows(Exception.class, () -> {
            viagemDAO.encerrarViagem(viagem.getId(), 29900.00);
        });

        assertTrue(exception.getMessage().contains("KM FINAL NAO PODE SER MENOR QUE O KM INICIAL"));

        // Garante que o estado não mudou
        Viagem naoEncerrada = viagemDAO.buscarPorId(viagem.getId());
        assertNull(naoEncerrada.getKmFinal());
        assertNull(naoEncerrada.getDataChegada());
        assertEquals("EM_ANDAMENTO", naoEncerrada.getStatus());
    }

    @Test
    @Order(6)
    public void testRejeicaoViagemInexistente() {
        assertThrows(IllegalArgumentException.class, () -> {
            viagemDAO.encerrarViagem(9999999, 10000.0);
        });
    }

    @Test
    @Order(7)
    public void testRollbackAposFalha() {
        String placa = "VIAG003";
        String cpf = "99911122233";

        Veiculo veic = veiculoDAO.buscarPorPlaca(placa);
        if (veic == null) {
            veic = new Veiculo();
            veic.setPlaca(placa);
            veic.setModelo("Caminhao Teste");
            veic.setAno(2020);
            veic.setQuilometragemAtual(50000.00);
            veic.setStatusVeiculo("DISPONIVEL");
            veic.setTipo("CAMINHAO");
            veiculoDAO.salvar(veic);
        }

        Motorista mot = motoristaDAO.buscarPorCpf(cpf);
        if (mot == null) {
            mot = new Motorista();
            mot.setCpf(cpf);
            mot.setNome("Motorista Caminhao");
            mot.setCategoriaCnh("E");
            motoristaDAO.salvar(mot);
        }

        Viagem viagem = new Viagem();
        viagem.setVeiculo(veic);
        viagem.setMotorista(mot);
        viagem.setDataSaida(LocalDateTime.now());
        viagem.setKmInicial(50000.00);
        viagem.setStatus("EM_ANDAMENTO");
        viagemDAO.salvar(viagem);

        em.getTransaction().begin();
        boolean erroCapturado = false;
        try {
            // Modifica veículo em memória/transação
            veic.setStatusVeiculo("MANUTENCAO");
            em.merge(veic);

            // Operação que falha na procedure
            viagemDAO.encerrarViagem(viagem.getId(), 40000.00);

            em.getTransaction().commit();
        } catch (Exception e) {
            erroCapturado = true;
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }

        assertTrue(erroCapturado);

        // Verifica que o rollback desfez a alteração do veículo
        em.clear();
        Veiculo veicConsultado = veiculoDAO.buscarPorPlaca(placa);
        assertEquals("DISPONIVEL", veicConsultado.getStatusVeiculo());
    }
}
