// Configurações
const API_BASE_URL = 'http://localhost:8080/api';
const API_URLS = {
    pessoas: `${API_BASE_URL}/pessoas`,
    doacoes: `${API_BASE_URL}/doacoes`,
    bancoSangue: `${API_BASE_URL}/banco-sangue`,
    cep: `${API_BASE_URL}/pessoas/cep`
};

// Estado global
let estado = {
    pessoas: [],
    doacoes: [],
    estoque: {},
    editandoPessoaId: null,
    editandoDoacaoId: null
};

// ========== FUNÇÕES GERAIS ==========

// Função para mostrar alertas
function mostrarAlerta(mensagem, tipo = 'success') {
    const alertElement = document.getElementById(`${tipo}Alert`);
    if (alertElement) {
        alertElement.textContent = mensagem;
        alertElement.classList.remove('hidden');
        setTimeout(() => {
            alertElement.classList.add('hidden');
        }, 5000);
    }
    console.log(`[${tipo.toUpperCase()}] ${mensagem}`);
}

// Função para fazer requisições HTTP
async function apiRequest(url, method, data = null) {
    const config = {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
    };

    if (data) {
        config.body = JSON.stringify(data);
    }

    try {
        console.log(`🌐 ${method} ${url}`, data);
        const response = await fetch(url, config);

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Erro ${response.status}: ${errorText}`);
        }

        // Para DELETE sem conteúdo
        if (response.status === 204) {
            return null;
        }

        const responseData = await response.json();
        return responseData;

    } catch (error) {
        console.error('Erro na requisição:', error);
        throw error;
    }
}

// Função para mostrar/ocultar abas
function showTab(tabName) {
    // Esconder todas as abas
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Remover active de todos os links da sidebar
    document.querySelectorAll('.sidebar-nav a').forEach(link => {
        link.classList.remove('active');
    });
    
    // Mostrar aba selecionada
    document.getElementById(`tab-${tabName}`).classList.add('active');
    
    // Ativar link da sidebar selecionado
    document.querySelector(`.sidebar-nav a[onclick="showTab('${tabName}')"]`).classList.add('active');
    
    // Atualizar título da página
    atualizarTituloPagina(tabName);
    
    // Carregar dados específicos da aba
    carregarDadosAba(tabName);
}

function atualizarTituloPagina(aba) {
    const titulos = {
        dashboard: 'Dashboard do Banco de Sangue',
        pessoas: 'Gestão de Pessoas e Doadores',
        doacoes: 'Registro e Gestão de Doações',
        estoque: 'Controle de Estoque Sanguíneo',
        compatibilidade: 'Verificação de Compatibilidade'
    };
    
    const descricoes = {
        dashboard: 'Visão geral do sistema',
        pessoas: 'Cadastro e consulta de doadores',
        doacoes: 'Registro e acompanhamento de doações',
        estoque: 'Monitoramento do estoque de sangue',
        compatibilidade: 'Verificação de compatibilidade sanguínea'
    };
    
    document.getElementById('pageTitle').textContent = titulos[aba] || 'Sistema Banco de Sangue';
    document.getElementById('pageDescription').textContent = descricoes[aba] || 'Sistema integrado de gestão';
}

function carregarDadosAba(aba) {
    switch(aba) {
        case 'dashboard':
            carregarDashboard();
            break;
        case 'pessoas':
            carregarPessoas();
            break;
        case 'doacoes':
            carregarDoacoes();
            break;
        case 'estoque':
            carregarEstoque();
            break;
        case 'compatibilidade':
            carregarCompatibilidade();
            break;
    }
}

// ========== DASHBOARD ==========

async function carregarDashboard() {
    try {
        console.log('📊 Carregando dashboard...');
        
        // Carregar estatísticas
        const estatisticas = await apiRequest(`${API_URLS.bancoSangue}/estatisticas`, 'GET');
        const estoque = await apiRequest(`${API_URLS.bancoSangue}/estoque`, 'GET');
        const estoqueBaixo = await apiRequest(`${API_URLS.bancoSangue}/estoque/baixo`, 'GET');
        const pessoas = await apiRequest(API_URLS.pessoas, 'GET');
        const doacoes = await apiRequest(API_URLS.doacoes, 'GET');
        
        // Atualizar estatísticas rápidas
        document.getElementById('totalEstoque').textContent = estatisticas.totalEstoque || 0;
        document.getElementById('totalDoadores').textContent = pessoas.length;
        document.getElementById('doacoesHoje').textContent = doacoes.filter(d => 
            new Date(d.dataDoacao).toDateString() === new Date().toDateString()
        ).length;
        document.getElementById('alertas').textContent = estoqueBaixo.length;
        
        // Atualizar gráfico de estoque
        atualizarGraficoEstoque(estoque);
        
        // Atualizar doações recentes
        atualizarDoacoesRecentes(doacoes.slice(0, 5));
        
        // Atualizar alertas
        atualizarAlertasEstoque(estoqueBaixo);
        
    } catch (error) {
        console.error('Erro ao carregar dashboard:', error);
        mostrarAlerta('Erro ao carregar dados do dashboard', 'error');
    }
}

function atualizarGraficoEstoque(estoque) {
    const container = document.getElementById('estoqueChart');
    container.innerHTML = '';
    
    Object.entries(estoque).forEach(([tipo, quantidade]) => {
        const card = document.createElement('div');
        card.className = `blood-type-card ${quantidade < 10 ? 'low' : ''}`;
        card.innerHTML = `
            <div class="type">${tipo.replace('_', '')}</div>
            <div class="quantity">${quantidade}</div>
            <div class="label">bolsas</div>
        `;
        container.appendChild(card);
    });
}

function atualizarDoacoesRecentes(doacoes) {
    const tbody = document.getElementById('doacoesRecentesTable');
    tbody.innerHTML = '';
    
    if (doacoes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;">Nenhuma doação recente</td></tr>';
        return;
    }
    
    doacoes.forEach(doacao => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${doacao.doadorNome || 'N/A'}</td>
            <td>${doacao.tipoSanguineo || 'N/A'}</td>
            <td>${new Date(doacao.dataDoacao).toLocaleDateString()}</td>
            <td>${doacao.status || 'N/A'}</td>
        `;
        tbody.appendChild(row);
    });
}

function atualizarAlertasEstoque(estoqueBaixo) {
    const container = document.getElementById('alertasContainer');
    
    if (estoqueBaixo.length === 0) {
        container.innerHTML = '<div class="alert alert-success"> Estoque em níveis normais</div>';
        return;
    }
    
    let alertasHTML = '<div class="alert alert-danger"><strong> Alertas de Estoque Baixo:</strong><ul style="margin-top: 10px; margin-left: 20px;">';
    estoqueBaixo.forEach(tipo => {
        alertasHTML += `<li>Tipo ${tipo.replace('_', '')}</li>`;
    });
    alertasHTML += '</ul></div>';
    
    container.innerHTML = alertasHTML;
}

// ========== GESTÃO DE PESSOAS ==========

async function carregarPessoas() {
    try {
        console.log('👥 Carregando pessoas...');
        estado.pessoas = await apiRequest(API_URLS.pessoas, 'GET');
        renderizarTabelaPessoas(estado.pessoas);
    } catch (error) {
        console.error('Erro ao carregar pessoas:', error);
        mostrarAlerta('Erro ao carregar lista de pessoas', 'error');
    }
}

function renderizarTabelaPessoas(pessoas) {
    const tbody = document.getElementById('peopleTableBody');
    tbody.innerHTML = '';
    
    if (pessoas.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">Nenhuma pessoa cadastrada</td></tr>';
        return;
    }
    
    pessoas.forEach(pessoa => {
        const telefone = pessoa.contatos?.find(c => c.tipo === 'TELEFONE' || c.tipo === 'WHATSAPP')?.valor || '-';
        const cidade = pessoa.endereco?.cidade || '-';
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${pessoa.nome}</td>
            <td>${pessoa.tipoSanguineo || '-'}</td>
            <td>${telefone}</td>
            <td>${cidade}</td>
            <td class="actions">
                <button class="btn btn-primary" onclick="editarPessoa(${pessoa.id})">Editar</button>
                <button class="btn btn-danger" onclick="excluirPessoa(${pessoa.id})">Excluir</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Formulário de Pessoa
document.getElementById('personForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    await salvarPessoa();
});

async function salvarPessoa() {
    try {
        const formData = obterDadosFormularioPessoa();
        
        if (estado.editandoPessoaId) {
            // Atualizar pessoa existente
            await apiRequest(`${API_URLS.pessoas}/${estado.editandoPessoaId}`, 'PUT', formData);
            mostrarAlerta('Pessoa atualizada com sucesso!');
        } else {
            // Criar nova pessoa
            await apiRequest(API_URLS.pessoas, 'POST', formData);
            mostrarAlerta('Pessoa cadastrada com sucesso!');
        }
        
        await carregarPessoas();
        resetarFormularioPessoa();
        
    } catch (error) {
        console.error('Erro ao salvar pessoa:', error);
        mostrarAlerta(`Erro ao salvar pessoa: ${error.message}`, 'error');
    }
}

function obterDadosFormularioPessoa() {
    const contatos = [];
    const documentos = [];
    
    // Contatos
    const telefone = document.getElementById('telefone').value;
    const email = document.getElementById('email').value;
    
    if (telefone) contatos.push({ tipo: 'TELEFONE', valor: telefone });
    if (email) contatos.push({ tipo: 'EMAIL', valor: email });
    
    // Documentos
    const rg = document.getElementById('rg').value;
    const cpf = document.getElementById('cpf').value;
    
    if (rg) documentos.push({ tipo: 'RG', numero: rg });
    if (cpf) documentos.push({ tipo: 'CPF', numero: cpf });
    
    return {
        nome: document.getElementById('nome').value,
        tipoSanguineo: document.getElementById('tipoSanguineo').value || null,
        endereco: {
            cep: document.getElementById('cep').value,
            logradouro: document.getElementById('logradouro').value,
            numero: document.getElementById('numero').value,
            complemento: document.getElementById('complemento').value,
            bairro: document.getElementById('bairro').value,
            cidade: document.getElementById('cidade').value,
            estado: document.getElementById('estado').value,
            pais: 'Brasil'
        },
        contatos: contatos,
        documentos: documentos,
        filiacao: {
            nomePai: document.getElementById('nomePai').value,
            nomeMae: document.getElementById('nomeMae').value
        }
    };
}

async function editarPessoa(id) {
    try {
        const pessoa = await apiRequest(`${API_URLS.pessoas}/${id}`, 'GET');
        preencherFormularioPessoa(pessoa);
        estado.editandoPessoaId = id;
        
        document.getElementById('pessoaFormTitle').textContent = 'Editar Pessoa';
        document.getElementById('submitPessoaBtn').textContent = 'Atualizar';
        document.getElementById('cancelPessoaBtn').style.display = 'inline-block';
        
        showTab('pessoas');
        
    } catch (error) {
        console.error('Erro ao carregar pessoa para edição:', error);
        mostrarAlerta('Erro ao carregar dados da pessoa', 'error');
    }
}

function preencherFormularioPessoa(pessoa) {
    document.getElementById('nome').value = pessoa.nome || '';
    document.getElementById('tipoSanguineo').value = pessoa.tipoSanguineo || '';
    
    // Endereço
    if (pessoa.endereco) {
        document.getElementById('cep').value = pessoa.endereco.cep || '';
        document.getElementById('logradouro').value = pessoa.endereco.logradouro || '';
        document.getElementById('numero').value = pessoa.endereco.numero || '';
        document.getElementById('complemento').value = pessoa.endereco.complemento || '';
        document.getElementById('bairro').value = pessoa.endereco.bairro || '';
        document.getElementById('cidade').value = pessoa.endereco.cidade || '';
        document.getElementById('estado').value = pessoa.endereco.estado || '';
    }
    
    // Contatos
    const telefone = pessoa.contatos?.find(c => c.tipo === 'TELEFONE' || c.tipo === 'WHATSAPP');
    const email = pessoa.contatos?.find(c => c.tipo === 'EMAIL');
    document.getElementById('telefone').value = telefone?.valor || '';
    document.getElementById('email').value = email?.valor || '';
    
    // Documentos
    const rg = pessoa.documentos?.find(d => d.tipo === 'RG');
    const cpf = pessoa.documentos?.find(d => d.tipo === 'CPF');
    document.getElementById('rg').value = rg?.numero || '';
    document.getElementById('cpf').value = cpf?.numero || '';
    
    // Filiação
    document.getElementById('nomePai').value = pessoa.filiacao?.nomePai || '';
    document.getElementById('nomeMae').value = pessoa.filiacao?.nomeMae || '';
}

async function excluirPessoa(id) {
    if (!confirm('Tem certeza que deseja excluir esta pessoa?')) return;
    
    try {
        await apiRequest(`${API_URLS.pessoas}/${id}`, 'DELETE');
        mostrarAlerta('Pessoa excluída com sucesso!');
        await carregarPessoas();
        
        // Se estava editando a pessoa excluída, resetar o formulário
        if (estado.editandoPessoaId === id) {
            resetarFormularioPessoa();
        }
    } catch (error) {
        console.error('Erro ao excluir pessoa:', error);
        mostrarAlerta('Erro ao excluir pessoa', 'error');
    }
}

function resetarFormularioPessoa() {
    document.getElementById('personForm').reset();
    estado.editandoPessoaId = null;
    document.getElementById('pessoaFormTitle').textContent = 'Cadastrar Pessoa';
    document.getElementById('submitPessoaBtn').textContent = 'Cadastrar';
    document.getElementById('cancelPessoaBtn').style.display = 'none';
}

function filtrarPessoas() {
    const termo = document.getElementById('searchPessoasInput').value.toLowerCase();
    const tipoFiltro = document.getElementById('filterBloodType').value;
    
    let pessoasFiltradas = estado.pessoas;
    
    if (termo) {
        pessoasFiltradas = pessoasFiltradas.filter(p => 
            p.nome.toLowerCase().includes(termo)
        );
    }
    
    if (tipoFiltro) {
        pessoasFiltradas = pessoasFiltradas.filter(p => 
            p.tipoSanguineo === tipoFiltro
        );
    }
    
    renderizarTabelaPessoas(pessoasFiltradas);
}

// Consulta CEP
async function consultarCEP() {
    const cep = document.getElementById('cep').value.replace(/\D/g, '');
    
    if (cep.length !== 8) {
        mostrarAlerta('CEP deve ter 8 dígitos', 'error');
        return;
    }
    
    try {
        const endereco = await apiRequest(`${API_URLS.cep}/${cep}`, 'GET');
        
        if (endereco) {
            document.getElementById('logradouro').value = endereco.logradouro || '';
            document.getElementById('bairro').value = endereco.bairro || '';
            document.getElementById('cidade').value = endereco.cidade || '';
            document.getElementById('estado').value = endereco.estado || '';
            document.getElementById('complemento').value = endereco.complemento || '';
            
            mostrarAlerta('Endereço preenchido automaticamente!');
        }
    } catch (error) {
        console.error('Erro ao consultar CEP:', error);
        mostrarAlerta('Erro ao consultar CEP. Preencha manualmente.', 'warning');
    }
}

// ========== GESTÃO DE DOAÇÕES ==========

async function carregarDoacoes() {
    try {
        console.log('💉 Carregando doações...');
        estado.doacoes = await apiRequest(API_URLS.doacoes, 'GET');
        renderizarTabelaDoacoes(estado.doacoes);
        atualizarSelectDoacoesParaEstoque();
    } catch (error) {
        console.error('Erro ao carregar doações:', error);
        mostrarAlerta('Erro ao carregar lista de doações', 'error');
    }
}

function renderizarTabelaDoacoes(doacoes) {
    const tbody = document.getElementById('doacoesTableBody');
    tbody.innerHTML = '';
    
    if (doacoes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Nenhuma doação registrada</td></tr>';
        return;
    }
    
    doacoes.forEach(doacao => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${doacao.doadorNome || 'N/A'}</td>
            <td>${doacao.tipoSanguineo || 'N/A'}</td>
            <td>${doacao.quantidade} ml</td>
            <td>${new Date(doacao.dataDoacao).toLocaleDateString()}</td>
            <td>
                <span class="status-${doacao.status?.toLowerCase()}">
                    ${doacao.status || 'N/A'}
                </span>
            </td>
            <td class="actions">
                ${doacao.status === 'DISPONIVEL' ? 
                    `<button class="btn btn-success" onclick="adicionarDoacaoEstoque(${doacao.id})">Adicionar ao Estoque</button>` : 
                    ''
                }
                <button class="btn btn-danger" onclick="excluirDoacao(${doacao.id})">Excluir</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Formulário de Doação
document.getElementById('doacaoForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    await salvarDoacao();
});

async function salvarDoacao() {
    try {
        const formData = {
            documentoDoador: document.getElementById('documentoDoador').value,
            quantidade: parseFloat(document.getElementById('quantidadeDoacao').value),
            dataDoacao: document.getElementById('dataDoacao').value || new Date().toISOString(),
            validoAte: document.getElementById('validoAteDoacao').value || null,
            status: 'DISPONIVEL'
        };
        
        await apiRequest(API_URLS.doacoes, 'POST', formData);
        mostrarAlerta('Doação registrada com sucesso!');
        
        await carregarDoacoes();
        document.getElementById('doacaoForm').reset();
        
    } catch (error) {
        console.error('Erro ao registrar doação:', error);
        mostrarAlerta(`Erro ao registrar doação: ${error.message}`, 'error');
    }
}

async function adicionarDoacaoEstoque(doacaoId) {
    try {
        await apiRequest(`${API_URLS.bancoSangue}/doacoes/${doacaoId}/adicionar-estoque`, 'POST');
        mostrarAlerta('Doação adicionada ao estoque com sucesso!');
        await carregarDoacoes();
        await carregarEstoque();
    } catch (error) {
        console.error('Erro ao adicionar doação ao estoque:', error);
        mostrarAlerta(`Erro ao adicionar doação ao estoque: ${error.message}`, 'error');
    }
    window.location.reload();
}

async function excluirDoacao(doacaoId) {
    if (!confirm('Tem certeza que deseja excluir esta doação?')) return;
    
    try {
        await apiRequest(`${API_URLS.doacoes}/${doacaoId}`, 'DELETE');
        mostrarAlerta('Doação excluída com sucesso!');
        await carregarDoacoes();
    } catch (error) {
        console.error('Erro ao excluir doação:', error);
        mostrarAlerta('Erro ao excluir doação', 'error');
    }
}

function filtrarDoacoes() {
    const termo = document.getElementById('searchDoacoesInput').value.toLowerCase();
    const statusFiltro = document.getElementById('filterStatusDoacao').value;
    
    let doacoesFiltradas = estado.doacoes;
    
    if (termo) {
        doacoesFiltradas = doacoesFiltradas.filter(d => 
            d.documentoDoador?.toLowerCase().includes(termo) ||
            d.doadorNome?.toLowerCase().includes(termo)
        );
    }
    
    if (statusFiltro) {
        doacoesFiltradas = doacoesFiltradas.filter(d => d.status === statusFiltro);
    }
    
    renderizarTabelaDoacoes(doacoesFiltradas);
}

function atualizarSelectDoacoesParaEstoque() {
    const select = document.getElementById('doacaoParaEstoque');
    select.innerHTML = '<option value="">Selecione uma doação disponível</option>';
    
    const doacoesDisponiveis = estado.doacoes.filter(d => d.status === 'DISPONIVEL');
    
    doacoesDisponiveis.forEach(doacao => {
        const option = document.createElement('option');
        option.value = doacao.id;
        option.textContent = `${doacao.doadorNome} - ${doacao.tipoSanguineo} - ${doacao.quantidade}ml`;
        select.appendChild(option);
    });
}

// ========== CONTROLE DE ESTOQUE ==========

async function carregarEstoque() {
    try {
        console.log('📦 Carregando estoque...');
        estado.estoque = await apiRequest(`${API_URLS.bancoSangue}/estoque`, 'GET');
        const estatisticas = await apiRequest(`${API_URLS.bancoSangue}/estatisticas`, 'GET');
        
        atualizarInterfaceEstoque(estado.estoque, estatisticas);
        
    } catch (error) {
        console.error('Erro ao carregar estoque:', error);
        mostrarAlerta('Erro ao carregar dados do estoque', 'error');
    }
}

function atualizarInterfaceEstoque(estoque, estatisticas) {
    // Atualizar grid de estoque
    const container = document.getElementById('estoqueAtual');
    container.innerHTML = '';
    
    Object.entries(estoque).forEach(([tipo, quantidade]) => {
        const card = document.createElement('div');
        card.className = `blood-type-card ${quantidade < 10 ? 'low' : ''}`;
        card.innerHTML = `
            <div class="type">${tipo.replace('_', '')}</div>
            <div class="quantity">${quantidade}</div>
            <div class="label">bolsas</div>
        `;
        container.appendChild(card);
    });
    
    // Atualizar estatísticas
    const statsContainer = document.getElementById('estatisticasEstoque');
    statsContainer.innerHTML = `
        <p><strong>Total em estoque:</strong> ${estatisticas.totalEstoque || 0} bolsas</p>
        <p><strong>Doações disponíveis:</strong> ${estatisticas.doacoesDisponiveis || 0}</p>
        <p><strong>Total de doações:</strong> ${estatisticas.totalDoacoes || 0}</p>
    `;
}

async function adicionarDoacaoAoEstoque() {
    const select = document.getElementById('doacaoParaEstoque');
    const doacaoId = select.value;
    
    if (!doacaoId) {
        mostrarAlerta('Selecione uma doação para adicionar ao estoque', 'warning');
        return;
    }
    
    try {
        await apiRequest(`${API_URLS.bancoSangue}/doacoes/${doacaoId}/adicionar-estoque`, 'POST');
        mostrarAlerta('Doação adicionada ao estoque com sucesso!');
        
        // Recarregar dados
        await carregarDoacoes();
        await carregarEstoque();
        
        // Resetar select
        select.value = '';
        
    } catch (error) {
        console.error('Erro ao adicionar doação ao estoque:', error);
        mostrarAlerta(`Erro ao adicionar doação ao estoque: ${error.message}`, 'error');
    }
    window.location.reload();
}

// ========== COMPATIBILIDADE ==========

async function carregarCompatibilidade() {
    console.log('🔄 Carregando dados de compatibilidade...');
    // A tabela de compatibilidade será gerada automaticamente
    gerarTabelaCompatibilidade();
}

function verificarCompatibilidade() {
    const tipoDoador = document.getElementById('tipoDoador').value;
    const tipoReceptor = document.getElementById('tipoReceptor').value;

    if (!tipoDoador || !tipoReceptor) {
        mostrarAlerta('Selecione ambos os tipos sanguíneos', 'warning');
        return;
    }

    const compatibilidades = {
        'A_POSITIVO': ['A_POSITIVO', 'A_NEGATIVO', 'O_POSITIVO', 'O_NEGATIVO'],
        'A_NEGATIVO': ['A_NEGATIVO', 'O_NEGATIVO'],
        'B_POSITIVO': ['B_POSITIVO', 'B_NEGATIVO', 'O_POSITIVO', 'O_NEGATIVO'],
        'B_NEGATIVO': ['B_NEGATIVO', 'O_NEGATIVO'],
        'AB_POSITIVO': ['A_POSITIVO','A_NEGATIVO','B_POSITIVO','B_NEGATIVO','AB_POSITIVO','AB_NEGATIVO','O_POSITIVO','O_NEGATIVO'],
        'AB_NEGATIVO': ['A_NEGATIVO','B_NEGATIVO','AB_NEGATIVO','O_NEGATIVO'],
        'O_POSITIVO': ['O_POSITIVO', 'O_NEGATIVO'],
        'O_NEGATIVO': ['O_NEGATIVO']
    };

    // Verificar compatibilidade simples
    const compativel = compatibilidades[tipoReceptor].includes(tipoDoador);

    const resultadoDiv = document.getElementById('resultadoCompatibilidade');
    resultadoDiv.className = `compatibility-result ${compativel ? 'compatible' : 'not-compatible'}`;
    resultadoDiv.innerHTML = compativel
        ? '✅ COMPATÍVEL - A doação pode ser realizada!'
        : '❌ INCOMPATÍVEL - Não é seguro realizar a doação';
    resultadoDiv.classList.remove('hidden');

    // Gerar tabela única
    gerarTabelaUnica(tipoDoador, tipoReceptor, compatibilidades);
}

function gerarTabelaUnica(tipoDoador, tipoReceptor, compatibilidades) {
    const tipos = Object.keys(compatibilidades);

    let html = `
        <h3>Tabela de Compatibilidade</h3>
        <p><strong>Doador selecionado:</strong> ${tipoDoador}</p>
        <p><strong>Receptor selecionado:</strong> ${tipoReceptor}</p>

        <table style="width:100%; text-align:center; margin-top:15px;">
            <thead>
                <tr>
                    <th>Tipo</th>
                    <th>Pode receber do DOADOR (${tipoDoador})?</th>
                    <th>Pode doar para o RECEPTOR (${tipoReceptor})?</th>
                </tr>
            </thead>
            <tbody>
    `;

    tipos.forEach(tipo => {
        const podeReceberDoDoador = compatibilidades[tipo].includes(tipoDoador);
        const podeDoarParaReceptor = compatibilidades[tipoReceptor].includes(tipo);

        html += `
            <tr>
                <td>${tipo}</td>
                <td class="${podeReceberDoDoador ? 'compativel' : 'incompativel'}">
                    ${podeReceberDoDoador ? '✓' : '✗'}
                </td>
                <td class="${podeDoarParaReceptor ? 'compativel' : 'incompativel'}">
                    ${podeDoarParaReceptor ? '✓' : '✗'}
                </td>
            </tr>
        `;
    });

    html += `
            </tbody>
        </table>
    `;

    document.getElementById('tabelaCompatibilidade').innerHTML = html;
}


// async function buscarDoadoresCompativeis() {
//     const tipoReceptor = document.getElementById('tipoReceptorBusca').value;
    
//     if (!tipoReceptor) {
//         document.getElementById('listaDoadoresCompativeis').innerHTML = '';
//         return;
//     }
    
//     try {
//         const doadores = await apiRequest(
//             `${API_URLS.bancoSangue}/doadores/compatíveis/${tipoReceptor}`, 
//             'GET'
//         );
        
//         const container = document.getElementById('listaDoadoresCompativeis');
        
//         if (doadores.length === 0) {
//             container.innerHTML = '<p>Nenhum doador compatível encontrado</p>';
//             return;
//         }
        
//         let html = '<div style="max-height: 300px; overflow-y: auto;"><table style="width: 100%;"><thead><tr><th>Nome</th><th>Tipo</th><th>Telefone</th></tr></thead><tbody>';
        
//         doadores.forEach(doador => {
//             const telefone = doador.contatos?.find(c => c.tipo === 'TELEFONE' || c.tipo === 'WHATSAPP')?.valor || '-';
//             html += `
//                 <tr>
//                     <td>${doador.nome}</td>
//                     <td>${doador.tipoSanguineo}</td>
//                     <td>${telefone}</td>
//                 </tr>
//             `;
//         });
        
//         html += '</tbody></table></div>';
//         container.innerHTML = html;
        
//     } catch (error) {
//         console.error('Erro ao buscar doadores compatíveis:', error);
//         mostrarAlerta('Erro ao buscar doadores compatíveis', 'error');
//     }
// }

function gerarTabelaCompatibilidade() {
    const tipos = ['A_POSITIVO', 'A_NEGATIVO', 'B_POSITIVO', 'B_NEGATIVO', 'AB_POSITIVO', 'AB_NEGATIVO', 'O_POSITIVO', 'O_NEGATIVO'];
    const compatibilidades = {
        'A_POSITIVO': ['A_POSITIVO', 'A_NEGATIVO', 'O_POSITIVO', 'O_NEGATIVO'],
        'A_NEGATIVO': ['A_NEGATIVO', 'O_NEGATIVO'],
        'B_POSITIVO': ['B_POSITIVO', 'B_NEGATIVO', 'O_POSITIVO', 'O_NEGATIVO'],
        'B_NEGATIVO': ['B_NEGATIVO', 'O_NEGATIVO'],
        'AB_POSITIVO': tipos, // Receptor universal
        'AB_NEGATIVO': ['A_NEGATIVO', 'B_NEGATIVO', 'AB_NEGATIVO', 'O_NEGATIVO'],
        'O_POSITIVO': ['O_POSITIVO', 'O_NEGATIVO'],
        'O_NEGATIVO': ['O_NEGATIVO'] // Doador universal
    };
    
    let html = `
        <table style="width: 100%; text-align: center;">
            <thead>
                <tr>
                    <th>Receptor →<br>Doador ↓</th>
                    ${tipos.map(tipo => `<th>${tipo.replace('_', '_')}</th>`).join('')}
                </tr>
            </thead>
            <tbody>
    `;
    
    tipos.forEach(doador => {
        html += `<tr><td><strong>${doador.replace('_', '_')}</strong></td>`;
        tipos.forEach(receptor => {
            const compativel = compatibilidades[receptor].includes(doador);
            html += `<td class="${compativel ? 'compativel' : 'incompativel'}">${compativel ? '✓' : '✗'}</td>`;
        });
        html += '</tr>';
    });
    
    html += '</tbody></table>';
    document.getElementById('tabelaCompatibilidade').innerHTML = html;
}

// ========== INICIALIZAÇÃO ==========

// Event Listeners
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Sistema Banco de Sangue inicializando...');
    
    // Botão cancelar pessoa
    document.getElementById('cancelPessoaBtn').addEventListener('click', resetarFormularioPessoa);
    
    // Botão cancelar doação
    document.getElementById('cancelDoacaoBtn').addEventListener('click', function() {
        document.getElementById('doacaoForm').reset();
        estado.editandoDoacaoId = null;
        document.getElementById('doacaoFormTitle').textContent = 'Registrar Doação';
        document.getElementById('submitDoacaoBtn').textContent = 'Registrar Doação';
        this.style.display = 'none';
    });
    
    // Carregar dados iniciais
    carregarDashboard();
    
    console.log(' Sistema inicializado com sucesso!');
});

// Função global para recarregar dashboard
function loadDashboardData() {
    carregarDashboard();
    mostrarAlerta('Dados atualizados!');
}