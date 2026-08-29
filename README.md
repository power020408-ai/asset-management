Web API開発で主流となっているSpring Bootの中に Spring Batchという機能があるのに興味を持ち、どのように実際の業務処理に適用できるのか知りたくなり、投資信託のバックオフィス業務で行われる基準価額計算を題材として今回の開発を試みました。またJavaの解説書のなかではあまり取り上げられることのないデータ型 BigDecimal についても、経験値をあげたく試作のなかで取り組みました。
<br/>

# 1. 実務要件
今回の開発では、以下の実務要件を満たすように設計しています。<br/>
(a) 資産データの数量・価格は日々変動し、外部CSVデータとしてインタフェースできること<br/>
(b) 同一日でも「約定通知遅延」「価格修正」が発生するため、資産データは日中でも上書き更新（UPSERT）できること<br/>
(c) 計算されたNAV、基準価額は日次で蓄積され、Webより履歴が参照可能なこと<br/>
(d) またファンド管理用Webから、CSVデータの取り込み、基準価額計算の起動が可能なこと<br/>
(e) 基準価額はスケジュール起動も可能とすること<br/>
<br/>

# 2. アーキテクチャ構成<br/>
Spring Boot の標準的なレイヤー構造を採用し、保守性・拡張性の高い設計になっています。<br/>
<br/>
<img width="242" height="226" alt="image" src="https://github.com/user-attachments/assets/1b6a13c7-03bb-4c5e-9004-07240488e07a" />
<br/>

# 3. 使用技術<br/>
<img width="501" height="226" alt="image" src="https://github.com/user-attachments/assets/eaa0fb26-a73e-4bd1-9b89-cb8ef2df4ba4" />
<br/>

# 4. エンティティ構造<br/>
(a) Fund（ファンド）<br/>
- ファンドの基本情報<br/>
- NAV、口数、基準価額の最新を保存<br/>
- Asset 及び FundNavHistory と(1:N)で紐付け<br/>

(b) FundNavHistory（基準価額履歴）<br/>

- 基準価額、NAV、口数を日次で保存
- NAV 計算時に自動で UPSERT
- Fund と紐付け

(c) Asset（資産）
- 複合主キー（fund_id, nav_date, asset_id）で保存
- 同一日・同一資産でも価格修正や約定通知遅延があるため、UPSERT で更新
- Fund と紐付け
<br/>

# 5. CSV インポート（UPSERT）<br/>
CSV をアップロードすることにより、Asset(資産)データを取り込みます。<br/>
(a) 特徴
   - 約定通知遅延や価格修正に対応
     同じ (fund_id, nav_date, asset_id) が存在する場合  →  UPDATE     
     存在しない場合   →   INSERT
        
(b) 処理の流れ
Spring Batch の Chunk モデルを使用し、以下の処理を実装しています。
   - JobOperator: /assets/upload (POST) により Batch 起動
   - ItemReader: Asset(資産) CSVデータを Javaクラスに取り込み
   - ItemProcesser: データベース格納用の Javaクラスに変換
   - ItemWriter: 変換後の JavaクラスをJDBCによりデータベースに格納
   - JobListener: Batch終了のステータスを画面に転送
<br/>

# 6. 基準価額計算<br/>
Spring Batch の Taskletモデルを使用し、JPAにより以下の処理を実装しています。<br/>
(a) 指定ファンドの資産をすべて取得<br/>
(b) NAV、口数、基準価額を算出<br/>
(c) FundNavHistory に保存（UPSERT）<br/>
(d) Fund（ファンド）のNAV、口数、基準価額を最新に更新<br/>
(e) ログ出力で計算過程を記録<br/>
またデータ型 BigDecimal を用いることにより丸め誤差による精度落ちに対処しつつ、端数切捨て・ゼロ判定にも適切に対応しています。
<br/>

# 7. 画面一覧<br/>

<img width="932" height="126" alt="image" src="https://github.com/user-attachments/assets/3d6df9ee-c5d0-432f-b301-8d641bd7ad9c" />


# 8. 実行方法

(a) PostgreSQL を起動

-　環境変数で DB ユーザー名・パスワードを設定：

            export DB_USER=your_user
   
            export DB_PASS=your_pass

(b) Spring Boot を起動

            ./mvnw spring-boot:run
<br/>

# 9. セキュリティ<br/>
以下の通り application.properties に DB パスワードを直接書かないようにしています。

            spring.datasource.username=${DB_USER}
            spring.datasource.password=${DB_PASS}
<br/>

# 10. 今後の拡張予定<br/>
今後、追加したい機能です。<br/>

- 資産分類（株式・債券・現金など）のため、銘柄テーブルを追加
- チャート表示（Chart.js）
- ファンドの複数クラス対応
- JSON形式のデータ開示用 Web APIの追加
<br/>
<br/>

## <参考>
<br/> 

### 画面 [ファンド管理]
<br/>
<img width="1354" height="477" alt="image" src="https://github.com/user-attachments/assets/bedcd635-e29c-4d12-b8b5-8711dc7ebc4d" />



### 画面 [ファンド開示 - ファンド一覧表]
<br/>
<img width="1332" height="442" alt="image" src="https://github.com/user-attachments/assets/a4bbf636-731c-4886-a6f7-b69208534b76" />



### 画面 [ファンド開示 - ファンド詳細]
<br/>
<img width="1366" height="720" alt="image" src="https://github.com/user-attachments/assets/5b6e4452-c995-4093-a5b8-e45470b31ff0" />
