import pandas as pd
import matplotlib.pyplot as plt
import os

def gerar_graficos_tempo(caminho_csv, nome_linguagem):
    pasta_saida = os.path.dirname(caminho_csv)
    
    try:
        df = pd.read_csv(caminho_csv)
    except Exception as e:
        print(f"  [ERRO] Não foi possível ler {caminho_csv}: {e}")
        return None

    tamanhos = sorted(df['tamanho_matriz'].unique())
    todas_stats = {}

    # 1. GERANDO OS GRÁFICOS INDIVIDUAIS DE TEMPO
    for tamanho in tamanhos:
        df_tam = df[df['tamanho_matriz'] == tamanho].copy()
        
        # Agrupando pelo número de threads e calculando média e desvio padrão do TEMPO
        stats = df_tam.groupby('threads')['tempo_segundos'].agg(['mean', 'std']).reset_index()
        stats['std'] = stats['std'].fillna(0)
        todas_stats[tamanho] = stats
        
        plt.figure(figsize=(10, 6))
        
        # Plotando a curva de tempo experimental
        plt.errorbar(
            x=stats['threads'], y=stats['mean'], yerr=stats['std'], 
            fmt='-o', color='#1f77b4', capsize=5, capthick=2, ecolor='#d62728', 
            markersize=8, linewidth=2, label=f'Tempo Real {nome_linguagem} ({tamanho}x{tamanho})'
        )
        
        # Calculando e plotando a curva de Tempo Ideal: T(p) = T(1) / p
        try:
            tempo_seq = df_tam[df_tam['threads'] == 1]['tempo_segundos'].mean()
            tempo_ideal = tempo_seq / stats['threads']
            plt.plot(stats['threads'], tempo_ideal, '--', color='gray', alpha=0.7, linewidth=2, label='Tempo Ideal (T1/p)')
        except:
            pass # Caso não tenha execução com 1 thread para calcular o ideal
        
        plt.title(f'Desempenho de Tempo ({nome_linguagem}) - Matriz {tamanho}x{tamanho}', fontsize=16, fontweight='bold', pad=15)
        plt.xlabel('Número de Threads', fontsize=14, labelpad=10)
        plt.ylabel('Tempo (Segundos)', fontsize=14, labelpad=10)
        plt.xticks(stats['threads'])
        plt.ylim(bottom=0)
        plt.xlim(left=0)
        plt.grid(True, linestyle='--', alpha=0.6)
        plt.legend(fontsize=12, loc='upper right') # Movido para a direita, já que a curva desce
        plt.tight_layout()
        
        caminho_img = os.path.join(pasta_saida, f'tempo_{tamanho}.png')
        plt.savefig(caminho_img, dpi=300, bbox_inches='tight')
        plt.close()

    # 2. GERANDO O GRÁFICO COMBINADO (TODOS OS TAMANHOS)
    plt.figure(figsize=(12, 8))
    cores = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd', '#8c564b']
    
    for i, tamanho in enumerate(tamanhos):
        stats = todas_stats[tamanho]
        cor = cores[i % len(cores)]
        
        plt.errorbar(
            x=stats['threads'], y=stats['mean'], yerr=stats['std'], 
            fmt='-o', color=cor, capsize=4, capthick=2, markersize=6, linewidth=2,
            label=f'Matriz {tamanho}x{tamanho}'
        )

    plt.title(f'Comparativo de Tempo ({nome_linguagem})', fontsize=16, fontweight='bold', pad=15)
    plt.xlabel('Número de Threads', fontsize=14, labelpad=10)
    plt.ylabel('Tempo (Segundos)', fontsize=14, labelpad=10)
    
    plt.xticks(todas_stats[tamanhos[0]]['threads'])
    plt.ylim(bottom=0)
    plt.xlim(left=0)
    plt.grid(True, linestyle='--', alpha=0.6)
    plt.legend(fontsize=12, loc='upper right')
    plt.tight_layout()
    
    caminho_combinado = os.path.join(pasta_saida, 'tempo_combinado.png')
    plt.savefig(caminho_combinado, dpi=300, bbox_inches='tight')
    plt.close()
    
    print(f"✅ Gráficos de TEMPO de {nome_linguagem} salvos em: {pasta_saida}/")
    
    # Retorna os dados processados para podermos usar no comparativo final C vs Java
    return todas_stats

def gerar_graficos_comparativos_tempo(dados_por_linguagem, dir_raiz):
    """
    Gera um gráfico para cada tamanho de matriz contendo a curva de C e de Java juntas focada no TEMPO.
    """
    pasta_saida = os.path.join(dir_raiz, 'output', 'comparativo_tempo')
    os.makedirs(pasta_saida, exist_ok=True)
    
    # Identifica os tamanhos de matriz que existem em ambas as linguagens
    tamanhos_c = set(dados_por_linguagem['C'].keys())
    tamanhos_java = set(dados_por_linguagem['JAVA'].keys())
    tamanhos_comuns = sorted(list(tamanhos_c.intersection(tamanhos_java)))
    
    if not tamanhos_comuns:
        print("\n  [AVISO] Não há tamanhos de matriz em comum entre C e Java para comparar.")
        return

    # Definindo cores padrão: Azul para C, Laranja para Java
    cores = {'C': '#1f77b4', 'JAVA': '#ff7f0e'}
    
    for tamanho in tamanhos_comuns:
        plt.figure(figsize=(10, 6))
        
        # Plota as curvas de ambas as linguagens
        for lang in ['C', 'JAVA']:
            stats = dados_por_linguagem[lang][tamanho]
            plt.errorbar(
                x=stats['threads'], y=stats['mean'], yerr=stats['std'], 
                fmt='-o', color=cores[lang], capsize=5, capthick=2, 
                markersize=8, linewidth=2, label=f'Tempo {lang}'
            )

        plt.title(f'Comparativo de Tempo em Segundos (C vs Java) - Matriz {tamanho}x{tamanho}', fontsize=16, fontweight='bold', pad=15)
        plt.xlabel('Número de Threads', fontsize=14, labelpad=10)
        plt.ylabel('Tempo (Segundos)', fontsize=14, labelpad=10)
        
        # Usa o eixo X baseado nas threads disponíveis (assumindo que C dita o padrão)
        plt.xticks(dados_por_linguagem['C'][tamanho]['threads'])
        
        plt.ylim(bottom=0)
        plt.xlim(left=0)
        plt.grid(True, linestyle='--', alpha=0.6)
        plt.legend(fontsize=12, loc='upper right') # Movido para a direita
        plt.tight_layout()
        
        caminho_img = os.path.join(pasta_saida, f'tempo_C_vs_Java_{tamanho}.png')
        plt.savefig(caminho_img, dpi=300, bbox_inches='tight')
        plt.close()
        
    print(f"\n✅ Gráficos comparativos de TEMPO (C vs Java) salvos em: {pasta_saida}/")

if __name__ == "__main__":
    dir_raiz = os.path.dirname(os.path.abspath(__file__))
    linguagens = ['c', 'java']
    
    dados_extraidos = {}
    
    print("=== Iniciando geração de gráficos focados no TEMPO ===")
    
    for lang in linguagens:
        caminho_csv = os.path.join(dir_raiz, 'output', lang, 'resultados.csv')
        
        if os.path.exists(caminho_csv):
            print(f"\n-> Processando dados da linguagem: {lang.upper()}")
            stats_calculadas = gerar_graficos_tempo(caminho_csv, lang.upper())
            
            # Salva no dicionário para a próxima etapa
            if stats_calculadas:
                dados_extraidos[lang.upper()] = stats_calculadas
        else:
            print(f"\n-> [AVISO] O arquivo {caminho_csv} ainda não existe. Pulando...")
            
    # Se conseguiu ler os dados de ambas as linguagens, gera o comparativo
    if 'C' in dados_extraidos and 'JAVA' in dados_extraidos:
        print("\n-> Gerando gráficos comparativos de tempo entre as linguagens...")
        gerar_graficos_comparativos_tempo(dados_extraidos, dir_raiz)
            
    print("\n=== Processamento concluído! ===")