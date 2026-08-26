Web API開発で主流となっているSpring Bootの中に Spring Batchという機能があるのに興味を持ち、どのように実際の業務処理に適用できるのか知りたくなり、投資信託のバックオフィス業務で行われる基準価格計算を題材として今回の開発を試みました。またJavaの解説書のなかであまり取り上げらることのないデータ型 BigDecimal についても、経験値をあげたく試作のなかで取り組みました。
<br/>
<br/>
#1. 実務要件  
今回の開発では、以下の実務要件を満たすように設計しています。  
(a) 資産データの数量・価格は日々変動し、今回は外部CSVデータとしてインタフェースできること  
(b) 同一日でも「約定通知遅延」「価格修正」が発生するため、資産データは日中でも上書き更新（UPSERT）できること  
(c) 計算されたNAV、基準価格は日次で蓄積され、Webより履歴が参照可能なこと  
(d) またファンド管理用Webから、CSVデータの取り込み、基準価格計算の起動が可能なこと  
(e) 基準価格計算はスケジュール起動も可能とすること
<br/>
<br/>
#2. アーキテクチャ構成<br/>
Spring Boot の標準的なレイヤー構造を採用し、保守性・拡張性の高い設計になっています。
　　
   <img width="242" height="226" alt="image" src="https://github.com/user-attachments/assets/1b6a13c7-03bb-4c5e-9004-07240488e07a" />
<br/>
<br/>
#3. 使用技術<br/>
   
  <img width="501" height="226" alt="image" src="https://github.com/user-attachments/assets/eaa0fb26-a73e-4bd1-9b89-cb8ef2df4ba4" />
<br/>
<br/>
#4. エンティティ構造<br/> 
(a) Fund（ファンド）   

- ファンドの基本情報
- NAV、口数、基準価額の最新を保存
- Asset 及び FundNavHistory と(1:N)で紐付け

(b) FundNavHistory（基準価額履歴） 

- 基準価額, NAV, 口数を日次で保存
- NAV 計算時に自動で UPSERT
- Fund と紐付け

(c) Asset（資産）
- 複合主キー（fund_id, nav_date, asset_id）で保存
- 同一日・同一資産でも価格修正や約定通知遅延があるため、UPSERT で更新
- Fund と紐付け
<br/>

#5. CSV インポート（UPSERT）</br>
CSV をアップロードすることにより、Asset(資産)データを取り込みます。<br>
(a) 特徴
   - 約定通知遅延や価格修正に対応  
     同じ (fund_id, nav_date, asset_id) が存在する場合  →  UPDATE     
     存在しない場合   →   INSERT
        
(b) 処理の流れ
   - /assets/upload (POST) によりCSV を読み込む
   - AssetId（複合キー）を生成
   - JPA Repository によりAsset（資産）を検索
   - 存在すればAsset（資産）を更新、なければ新規作成
<br/>
#6. 基準価額計算<br/>
Spring Batch により以下の処理を実装しています。<br/>
(a) 指定ファンドの資産をすべて取得<br/>
(b) NAV、口数、基準価額を算出<br/>
(c) FundNavHistory に保存（UPSERT）<br/>
(d) Fund（ファンド）のNAV、口数、基準価額を最新に更新<br/>
(e) ログ出力で計算過程を記録<br/>
<br/>
#7. Web AP 一覧<br/>
<img width="485" height="126" alt="image" src="https://github.com/user-attachments/assets/120a09a7-86ec-4f88-98fe-1ac0d2618236" />
<br/>
<br/>
#8. 実行方法

(a) PostgreSQL を起動
   
-　環境変数で DB ユーザー名・パスワードを設定：
   
            export DB_USER=your_user
   
            export DB_PASS=your_pass
   
(b) Spring Boot を起動
   
            ./mvnw spring-boot:run
   
#9. セキュリティ注意点
以下の通り application.properties に DB パスワードを直接書かないようにしています。

            spring.datasource.username=${DB_USER}
            spring.datasource.password=${DB_PASS}

            
#10. 今後の拡張予定
今後、追加したい機能です。
- 資産分類（株式・債券・現金など）のため、銘柄テーブルを追加
- チャート表示（Chart.js）
- ファンドの複数クラス対応
- JSON形式のデータ開示用 Web APIの追加
