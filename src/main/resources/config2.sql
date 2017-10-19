
select * from RT_ANALISYS order by no
select * from company order by no
delete from company where name = '에이블씨엔씨'
delete from stock where com_no = '485'
drop table stock
drop table opi_pos_dic
drop table opi_neg_dic
drop table opi_neu_dic
drop table pro2_dic

insert into company
values(company_seq.nextVal, '에이블씨엔씨', '078520.ks', 'kospi')

update company
set code = '002790.ks'
where no = 29

select RT_ANALISYS.no, company.name as comName, ana_code as anaCode, news_code as newsCode, today_fluc as todayFluc, tomorrow_fluc as tomorrowFluc, reg_date as regDate
from RT_ANALISYS, company
where RT_ANALISYS.com_no = company.no and company.name = '삼성전자'

select * from ANA_RELIABILITY
select *
from(
	select *
	from(
		select *
		from ANA_RELIABILITY
		where com_name = '남양유업'
		order by value desc
	)
)
where rownum=1
select * from users
select * from MY_ANALISYS order by no
select * from my_favorite where no> 25
select news_code, max(yesterday_fluc) from RT_ANALISYS  group by news_code  where news_code = 'digital'
select * from news_count
insert into MY_FAVORITE(NO, user_no, com_no, group_name) 
values(MY_FAVORITE_SEQ.nextVal, 2, 18, '보유주식')


select count(my2.com_no)
from (select no, user_no, com_no from my_stock where no = 1) my1, my_stock my2
where my1.user_no = my2.user_no and my1.com_no = my2.com_no