import pandas as pd
import matplotlib.pyplot as plt
import os

def gerar_graficos(caminho_csv, nome_linguagem):
    pasta_saida = os.path.dirname(caminho_csv)
    
    try:
        df = pd.read_csv(caminho_csv)
    except Exception as e:
        print(f"  [ERRO] Não foi possível ler {caminho_csv}: {e}")
        return

    tamanhos = sorted(df['tamanho_matriz'].unique())
    todas_stats = {}

    # 1. GERANDO OS GRÁFICOS INDIVIDUAIS
    for tamanho in tamanhos:
        df_tam = df[df['tamanho_matriz'] == tamanho].copy()
        
        tempo_seq = df_tam[df_tam['threads'] == 1]['tempo_segundos'].mean()
        df_tam['speedup'] = tempo_seq / df_tam['tempo_segundos']
        
        stats = df_tam.groupby('threads')['speedup'].agg(['mean', 'std']).reset_index()
        stats['std'] = stats['std'].fillna(0)
        todas_stats[tamanho] = stats
        
        plt.figure(figsize=(10, 6))
        plt.errorbar(
            x=stats['threads'], y=stats['mean'], yerr=stats['std'], 
            fmt='-o', color='#1f77b4', capsize=5, capthick=2, ecolor='#d62728', 
            markersize=8, linewidth=2, label=f'Experimental {nome_linguagem} ({tamanho}x{tamanho})'
        )
        
        max_threads = stats['threads'].max()
        plt.plot([1, max_threads], [1, max_threads], '--', color='gray', alpha=0.7, linewidth=2, label='Linear (Ideal)')
        
        # Título dinâmico indicando se é C ou Java
        plt.title(f'Curva de Speedup ({nome_linguagem}) - Matriz {tamanho}x{tamanho}', fontsize=16, fontweight='bold', pad=15)
        plt.xlabel('Número de Threads', fontsize=14, labelpad=10)
        plt.ylabel('Speedup', fontsize=14, labelpad=10)
        plt.xticks(stats['threads'])
        plt.ylim(bottom=0)
        plt.xlim(left=0)
        plt.grid(True, linestyle='--', alpha=0.6)
        plt.legend(fontsize=12, loc='upper left')
        plt.tight_layout()
        
        caminho_img = os.path.join(pasta_saida, f'speedup_{tamanho}.png')
        plt.savefig(caminho_img, dpi=300, bbox_inches='tight')
        plt.close()

    # 2. GERANDO O GRÁFICO COMBINADO
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

    max_threads_global = max([s['threads'].max() for s in todas_stats.values()])
    plt.plot([1, max_threads_global], [1, max_threads_global], '--', color='gray', alpha=0.7, linewidth=2, label='Linear (Ideal)')

    plt.title(f'Comparativo de Speedup ({nome_linguagem})', fontsize=16, fontweight='bold', pad=15)
    plt.xlabel('Número de Threads', fontsize=14, labelpad=10)
    plt.ylabel('Speedup', fontsize=14, labelpad=10)
    
    plt.xticks(todas_stats[tamanhos[0]]['threads'])
    plt.ylim(bottom=0)
    plt.xlim(left=0)
    plt.grid(True, linestyle='--', alpha=0.6)
    plt.legend(fontsize=12, loc='upper left')
    plt.tight_layout()
    
    caminho_combinado = os.path.join(pasta_saida, 'speedup_combinado.png')
    plt.savefig(caminho_combinado, dpi=300, bbox_inches='tight')
    plt.close()
    
    print(f"✅ Gráficos do {nome_linguagem} salvos em: {pasta_saida}/")

if __name__ == "__main__":
    dir_raiz = os.path.dirname(os.path.abspath(__file__))
    
    # Define as duas pastas de linguagens que queremos processar
    linguagens = ['c', 'java']
    
    print("=== Iniciando geração de gráficos ===")
    
    for lang in linguagens:
        # Força o script a procurar EXATAMENTE o arquivo "resultados.csv" na pasta da linguagem
        caminho_csv = os.path.join(dir_raiz, 'output', lang, 'resultados.csv')
        
        if os.path.exists(caminho_csv):
            print(f"\n-> Processando dados da linguagem: {lang.upper()}")
            gerar_graficos(caminho_csv, lang.upper())
        else:
            print(f"\n-> [AVISO] O arquivo {caminho_csv} ainda não existe. Pulando...")
            
    print("\n=== Processamento concluído! ===")