-- 테이블 순서는 관계를 고려하여 한 번에 실행해도 에러가 발생하지 않게 정렬되었습니다.

-- COMPANY Table Create SQL
CREATE TABLE COMPANY
(
    no            NUMBER          NOT NULL, 
    name          VARCHAR2(40)    NOT NULL, 
    code          VARCHAR2(20)    NOT NULL, 
    stock_type    VARCHAR2(5)     NOT NULL, 
    CONSTRAINT COMPANY_PK PRIMARY KEY (no)
)
/

CREATE SEQUENCE COMPANY_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;

-- NEWS_CATEGORY Table Create SQL
CREATE TABLE NEWS_CATEGORY
(
    no      NUMBER          NOT NULL, 
    name    VARCHAR2(6)     NOT NULL, 
    code    VARCHAR2(10)    NOT NULL, 
    CONSTRAINT NEWS_CATEGORY_PK PRIMARY KEY (no)
)
/
ALTER TABLE NEWS_CATEGORY
    ADD CONSTRAINT NEWS_CATEGORY_CODE_UNIQUE UNIQUE(code)
    
CREATE SEQUENCE NEWS_CATEGORY_SEQ
START WITH 1
INCREMENT BY 1;
/

-- USERS Table Create SQL
CREATE TABLE USERS
(
    no          NUMBER          NOT NULL, 
    id          VARCHAR2(20)    NOT NULL, 
    domain      VARCHAR2(20)    NOT NULL, 
    password    VARCHAR2(20)    NOT NULL, 
    name        VARCHAR2(30)    NOT NULL, 
    tel         VARCHAR2(13)    NOT NULL, 
    reg_date    DATE            NOT NULL, 
    CONSTRAINT USERS_PK PRIMARY KEY (no)
)
/

CREATE SEQUENCE USERS_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/

-- ANA_CATEGORY Table Create SQL
CREATE TABLE ANA_CATEGORY
(
    NO      INT             NOT NULL, 
    name    VARCHAR2(13)    NOT NULL, 
    code    VARCHAR2(4)     NOT NULL, 
    CONSTRAINT ANA_CATEGORY_PK PRIMARY KEY (NO)
)

ALTER TABLE ANA_CATEGORY
    ADD CONSTRAINT ANA_CATEGORY_CODE_UNIQUE UNIQUE(code)
/
CREATE SEQUENCE ANA_CATEGORY_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/

-- MY_DICTIONARY Table Create SQL
CREATE TABLE MY_DICTIONARY
(
    NO         NUMBER          NOT NULL, 
    user_no    NUMBER          NOT NULL, 
    com_no     NUMBER          NOT NULL, 
    name       VARCHAR(30)     NOT NULL, 
    term       VARCHAR2(90)    NOT NULL, 
    CONSTRAINT MY_DICTIONARY_PK PRIMARY KEY (NO)
)
/

CREATE SEQUENCE MY_DICTIONARY_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/


ALTER TABLE MY_DICTIONARY
    ADD CONSTRAINT FK_MY_DICTIONARY_user_no_USERS FOREIGN KEY (user_no)
        REFERENCES USERS (no)
/

ALTER TABLE MY_DICTIONARY
    ADD CONSTRAINT FK_MY_DICTIONARY_com_no_COMPAN FOREIGN KEY (com_no)
        REFERENCES COMPANY (no)
/


-- TFIDF Table Create SQL
CREATE TABLE TFIDF
(
    no           NUMBER          NOT NULL, 
    news_code    VARCHAR2(10)    NOT NULL, 
    term         VARCHAR2(90)    NOT NULL, 
    f            NUMBER          NOT NULL, 
    tf           NUMBER          NOT NULL, 
    df           NUMBER          NOT NULL, 
    idf          NUMBER          NOT NULL, 
    tfidf        NUMBER          NOT NULL, 
    CONSTRAINT TFIDF_PK PRIMARY KEY (no)
)

/
CREATE SEQUENCE TFIDF_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/

ALTER TABLE TFIDF
    ADD CONSTRAINT FK_TFIDF_news_code FOREIGN KEY (news_code)
        REFERENCES NEWS_CATEGORY (code)
/

drop table pro2_DIC
drop SEQUENCE pro2_DIC_SEQ
-- OPI_POS_DIC Table Create SQL
CREATE TABLE OPI_POS_DIC
(
    no           NUMBER          NOT NULL, 
    com_no       NUMBER          NOT NULL, 
    news_code    VARCHAR2(10)    NOT NULL, 
    term         VARCHAR2(90)    NOT NULL, 
    weight       NUMBER          NOT NULL, 
    CONSTRAINT OPI_POS_DIC_PK PRIMARY KEY (no)
)
/
CREATE SEQUENCE OPI_POS_DIC_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/
/

ALTER TABLE OPI_POS_DIC
    ADD CONSTRAINT FK_OPI_POS_DIC_com_no_COMPANY_ FOREIGN KEY (com_no)
        REFERENCES COMPANY (no)
/

ALTER TABLE OPI_POS_DIC
    ADD CONSTRAINT FK_OPI_POS_DIC_news_code_NEWS_ FOREIGN KEY (news_code)
        REFERENCES NEWS_CATEGORY (code)
/


-- OPI_NEG_DIC Table Create SQL
CREATE TABLE OPI_NEG_DIC
(
    no           NUMBER          NOT NULL, 
    com_no       NUMBER          NOT NULL, 
    news_code    VARCHAR2(10)    NOT NULL, 
    term         VARCHAR2(90)    NOT NULL, 
    weight       NUMBER          NOT NULL, 
    CONSTRAINT OPI_NEG_DIC_PK PRIMARY KEY (no)
)
/

CREATE SEQUENCE OPI_NEG_DIC_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/

ALTER TABLE OPI_NEG_DIC
    ADD CONSTRAINT FK_OPI_NEG_DIC_com_no_COMPANY_ FOREIGN KEY (com_no)
        REFERENCES COMPANY (no)
/

ALTER TABLE OPI_NEG_DIC
    ADD CONSTRAINT FK_OPI_NEG_DIC_news_code_NEWS_ FOREIGN KEY (news_code)
        REFERENCES NEWS_CATEGORY (code)
/


-- OPI_NEU_DIC Table Create SQL
CREATE TABLE OPI_NEU_DIC
(
    no           NUMBER          NOT NULL, 
    com_no       NUMBER          NOT NULL, 
    news_code    VARCHAR2(10)    NOT NULL, 
    term         VARCHAR2(90)    NOT NULL, 
    weight       NUMBER          NOT NULL, 
    CONSTRAINT OPI_NEU_DIC_PK PRIMARY KEY (no)
)
/

CREATE SEQUENCE OPI_NEU_DIC_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/

ALTER TABLE OPI_NEU_DIC
    ADD CONSTRAINT FK_OPI_NEU_DIC_com_no_COMPANY_ FOREIGN KEY (com_no)
        REFERENCES COMPANY (no)
/

ALTER TABLE OPI_NEU_DIC
    ADD CONSTRAINT FK_OPI_NEU_DIC_news_code_NEWS_ FOREIGN KEY (news_code)
        REFERENCES NEWS_CATEGORY (code)
/


-- PRO_DIC Table Create SQL
CREATE TABLE PRO_DIC
(
    no           NUMBER          NOT NULL, 
    com_no       NUMBER          NOT NULL, 
    news_code    VARCHAR2(10)    NOT NULL, 
    term         VARCHAR2(90)    NOT NULL, 
    inc          NUMBER          NOT NULL, 
    dec          NUMBER          NOT NULL, 
    equ          NUMBER          NOT NULL, 
    CONSTRAINT PRO_DIC_PK PRIMARY KEY (no)
)
/
CREATE SEQUENCE PRO_DIC_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/

ALTER TABLE PRO_DIC
    ADD CONSTRAINT FK_PRO_DIC_com_no_COMPANY_no FOREIGN KEY (com_no)
        REFERENCES COMPANY (no)
/

ALTER TABLE PRO_DIC
    ADD CONSTRAINT FK_PRO_DIC_news_code_NEWS_CATE FOREIGN KEY (news_code)
        REFERENCES NEWS_CATEGORY (code)
/


-- PRO2_DIC Table Create SQL
CREATE TABLE PRO2_DIC
(
    no           NUMBER          NOT NULL, 
    com_no       NUMBER          NOT NULL, 
    news_code    VARCHAR2(10)    NOT NULL, 
    term         VARCHAR2(90)    NOT NULL, 
    inc          NUMBER          NOT NULL, 
    dec          NUMBER          NOT NULL, 
    equ          NUMBER          NOT NULL, 
    CONSTRAINT PRO2_DIC_PK PRIMARY KEY (no)
)
/

CREATE SEQUENCE PRO2_DIC_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/

ALTER TABLE PRO2_DIC
    ADD CONSTRAINT FK_PRO2_DIC_com_no_COMPANY_no FOREIGN KEY (com_no)
        REFERENCES COMPANY (no)
/

ALTER TABLE PRO2_DIC
    ADD CONSTRAINT FK_PRO2_DIC_news_code_NEWS_CAT FOREIGN KEY (news_code)
        REFERENCES NEWS_CATEGORY (code)
/

-- STOCK Table Create SQL
CREATE TABLE STOCK
(
    NO            NUMBER     NOT NULL, 
    com_no        NUMBER     NOT NULL, 
    open_date     DATE       NOT NULL, 
    open_price          NUMBER     NOT NULL, 
    close_price         NUMBER     NOT NULL, 
    hig_priceh          NUMBER     NOT NULL, 
    low_price           NUMBER     NOT NULL, 
    volume        NUMBER     NOT NULL, 
    fluc_state    CHAR(1)    NOT NULL, 
    raise         NUMBER     NULL, 
    rate          NUMBER     NULL, 
    CONSTRAINT STOCK_PK PRIMARY KEY (NO)
)
/
CREATE SEQUENCE STOCK_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/

ALTER TABLE STOCK
    ADD CONSTRAINT FK_STOCK_com_no_COMPANY_no FOREIGN KEY (com_no)
        REFERENCES COMPANY (no)
/


-- MY_FAVORITE Table Create SQL
CREATE TABLE MY_FAVORITE
(
    NO         NUMBER    NOT NULL, 
    user_no    NUMBER    NOT NULL, 
    com_no     NUMBER    NOT NULL, 
    CONSTRAINT MY_FAVORITE_PK PRIMARY KEY (NO)
)
/

CREATE SEQUENCE MY_FAVORITE_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/

ALTER TABLE MY_FAVORITE
    ADD CONSTRAINT FK_MY_FAVORITE_user_no_USERS_n FOREIGN KEY (user_no)
        REFERENCES USERS (no)
/

ALTER TABLE MY_FAVORITE
    ADD CONSTRAINT FK_MY_FAVORITE_com_no_COMPANY_ FOREIGN KEY (com_no)
        REFERENCES COMPANY (no)
/


-- MY_STOCK Table Create SQL
CREATE TABLE MY_STOCK
(
    NO           NUMBER    NOT NULL, 
    user_no      NUMBER    NOT NULL, 
    com_no       NUMBER    NOT NULL, 
    buy_price    NUMBER    NOT NULL, 
    volume       NUMBER    NOT NULL, 
    buy_date     DATE      NULL, 
    CONSTRAINT MY_STOCK_PK PRIMARY KEY (NO)
)
/

CREATE SEQUENCE MY_STOCK_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/

ALTER TABLE MY_STOCK
    ADD CONSTRAINT FK_MY_STOCK_com_no_COMPANY_no FOREIGN KEY (com_no)
        REFERENCES COMPANY (no)
/

ALTER TABLE MY_STOCK
    ADD CONSTRAINT FK_MY_STOCK_user_no_USERS_no FOREIGN KEY (user_no)
        REFERENCES USERS (no)
/


-- RT_ANALISYS Table Create SQL
CREATE TABLE RT_ANALISYS
(
    no               NUMBER          NOT NULL, 
    com_no           NUMBER          NOT NULL, 
    ana_code         VARCHAR2(4)     NOT NULL, 
    news_code        VARCHAR2(10)    NOT NULL, 
    today_fluc       CHAR(1)         NOT NULL, 
    tomorrow_fluc    CHAR(1)         NOT NULL, 
    CONSTRAINT RT_ANALISYS_PK PRIMARY KEY (no)
)
/

CREATE SEQUENCE RT_ANALISYS_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/
ALTER TABLE RT_ANALISYS
    ADD CONSTRAINT FK_RT_ANALISYS_ana_code_ANA_CA FOREIGN KEY (ana_code)
        REFERENCES ANA_CATEGORY (code)
/

ALTER TABLE RT_ANALISYS
    ADD CONSTRAINT FK_RT_ANALISYS_news_code_NEWS_ FOREIGN KEY (news_code)
        REFERENCES NEWS_CATEGORY (code)
/


-- MY_ANALISYS Table Create SQL
CREATE TABLE MY_ANALISYS
(
    NO           NUMBER         NOT NULL, 
    user_no      NUMBER         NOT NULL, 
    my_dic_no    NUMBER         NOT NULL, 
    ana_code     VARCHAR2(4)    NOT NULL, 
    name         VARCHAR(30)    NOT NULL, 
    reg_date     DATE           NOT NULL, 
    CONSTRAINT MY_ANALISYS_PK PRIMARY KEY (NO)
)
/

CREATE SEQUENCE MY_ANALISYS_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/

ALTER TABLE MY_ANALISYS
    ADD CONSTRAINT FK_MY_ANALISYS_user_no_USERS_n FOREIGN KEY (user_no)
        REFERENCES USERS (no)
/

ALTER TABLE MY_ANALISYS
    ADD CONSTRAINT FK_MY_ANALISYS_my_dic_no_MY_DI FOREIGN KEY (my_dic_no)
        REFERENCES MY_DICTIONARY (NO)
/

ALTER TABLE MY_ANALISYS
    ADD CONSTRAINT FK_MY_ANALISYS_ana_code_ANA_CA FOREIGN KEY (ana_code)
        REFERENCES ANA_CATEGORY (code)
/


-- DECISION_TREE Table Create SQL
CREATE TABLE DECISION_TREE
(
    NO           NUMBER           NOT NULL, 
    com_no       NUMBER           NOT NULL, 
    ana_code     VARCHAR2(4)      NOT NULL, 
    news_code    VARCHAR2(10)     NOT NULL, 
    decision     VARCHAR2(100)    NOT NULL, 
    CONSTRAINT DECISION_TREE_PK PRIMARY KEY (NO)
)
/

CREATE SEQUENCE DECISION_TREE_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/


ALTER TABLE DECISION_TREE
    ADD CONSTRAINT FK_DECISION_TREE_com_no_COMPAN FOREIGN KEY (com_no)
        REFERENCES COMPANY (no)
/

ALTER TABLE DECISION_TREE
    ADD CONSTRAINT FK_DECISION_TREE_ana_code_ANA_ FOREIGN KEY (ana_code)
        REFERENCES ANA_CATEGORY (code)
/

ALTER TABLE DECISION_TREE
    ADD CONSTRAINT FK_DECISION_TREE_news_code_NEW FOREIGN KEY (news_code)
        REFERENCES NEWS_CATEGORY (code)
/


-- LINK Table Create SQL
CREATE TABLE LINK
(
    NO      NUMBER           NOT NULL, 
    name    VARCHAR2(200)    NOT NULL, 
    url     VARCHAR2(200)    NOT NULL, 
    CONSTRAINT LINK_PK PRIMARY KEY (NO)
)
/

CREATE SEQUENCE LINK_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE;
/

//////////////////////////////////////////////////////////////////////////////
select stock.no, com_no as comNo, open_date as openDate, open, close, high, low, volume, fluc_state as flucState, raise, rate
from stock, (select no from company where name='AK홀딩스') company where stock.com_no = company.no
select term, weight
from OPI_POS_DIC, (select no from company where name='AK홀딩스') company
where OPI_POS_DIC.com_no = company.no and news_code = 'economic'

select * from ana_category order by no

//
create index PRO_DIC_idx on PRO_DIC(com_no,news_code,term);

select avg(inc*tfidf.tfidf), avg(dec*tfidf.tfidf), avg(equ * tfidf.tfidf)
from (select com_no, term, inc, dec, equ from PRO_DIC where news_code = 'economic' 
and term in('재건축','아파트','서울시','단지','뉴스','주민','추진','주공','마','최고','잠실','계획','은','종','층수','높이','입장','압구정','가나','초고','위원회','연합뉴스','서울','전체','말','라마','허용','한강변','조합','다','바사','서비스','의견','국제','이하','다음','상향','심의','연예','대통령','바로가기','경제','주거','포토','관련','지구','주거지역','일반','사이','검색','내용','기사','단위','총회','대치동','전주','현대','결정','가구','조절','경우','이슈','원칙','문화','랭킹','입지','재검토','관계자','광역','위','자료','수용','사실','연재','건축','강릉','레이어','플랜','마광수','강경준','인천','공모','안내','제주','실시간','검','책임자','독도','관리','여성','울산','추가','대전','제출','사회 정치','선','전경','고수','뉴스 1','안','입력','서초구','미디어랩','여수','울릉','시설','청주','건설업','신고','춘천','광주','설명회','창원','공사','홈','사항','결과','기능','목포','대구','사진','사업','장신영','조선','메뉴','계획안','반포','시','언론사','복합','반려','저작권','부산','현상','스토리','도시계획','대치','이례','크기','정보','센터','음성','안동','개발','수원','스포츠','입주민','배열','확인','중심','문제','쭈쭈바','일정','북핵','무죄','발견','소개','권재홍','페이스북','마에다','스카이라인','정부','키워드','검색어','침해','앵커','해당','사업비','유의','상태','잠실역','배','전재','백','지역','설계','방문','최근','창','배치','벌금형','트위터','곤잘레스','이진숙','문재인','노트','도입','부지','가이드라인','진행','임선영','형평성','월경','남북관계','확정','전제','태그','전재용','통장','풍','미사일','법','정현','동안','지난달','개설','날씨','뒤','델 포트로','김상조','위문','금지','엘리스','서울시장','시간','차량','비밀번호','벽','시각','사전','잔여','시공사','김연정','구경','기준','고립','부활','블로그','정비','연결','기본','공유','확산','수정','중학교','좌절','재판','차례','수렴','차범근','경기','여부','오피스','지난해','극동','카카오스토리','그간','서','속보','확보','상징','주목','검토','미숙','직원','당초','치킨게임','컨벤션','요구','김호곤','추진위','교사','도시기본계획','반면','때','불만','인쇄','홍준표','본문','불허','댓글','계속','한','준비','통로','앞','애','함','5위','의지','이주노','토대','글자','택시','포럼','속','추천','인근','일부','정당','김혜자','종합','남성','감독','가운데','글쓴이','사드','기여','배포','금나나','규정','방문진','이용','잠원동','대부분','권리','파문','노력','석유','조정','봉추','청소년','외교','블랙리스트','단독','차주','울분','운영','옵션','이달','요금','지원금','관심','광고','역기','조합원','반대','사거리','구영','여명','영','혁','할인','선물','호텔','구역','경유','업무','발목','용도','무단','그동안','쿼트','반응','중심지','층','법정','과장','상대','엉덩이','지정','홍','통합','비전','서울 송파구','노무현','칼','제휴','소요','차두리','북방정책','용도지역','토로','사망','이승원','가스','공공','전도연','버스','수립','투입','윤여정','정책','단계','후디','분위기','지검','마고 로비','생각','링크','신','초고층 건물','부족','장현','백지영','벌금','야구부','희생','선정','영상','박원순','위증','보행','연면적','소설가','이전','완료','출석','단서','건설','강남','고객','이력','홍기섭','향배','서장훈','약관','글씨','신돌석','축구협회','문자','혐의','헬스장','원점','내부','주상','다리','자초','노역','동방','신천동','상정','기자','인','사이드','복지','협의','공지','한국','권장','나머지','직설','이견','이사','진주','검찰','송파구','예산','메인','카페','운동','메일','이병','책임','꿈','스포츠 스포츠','철거','김해숙','숙원','보호','발표','여중생','동','건립','영향','포기','하순','게시판','공정위','주구','변경','이영표','희망','러','북한','바른','미','폼')) PRO_DIC, 
(select no from company where name='AK홀딩스') company, 
(select term, tfidf from tfidf where news_code = 'economic' and 0 < IDF and IDF < 5)   tfidf
where PRO_DIC.com_no = company.no and PRO_DIC.term = tfidf.term

select * from tfidf 
