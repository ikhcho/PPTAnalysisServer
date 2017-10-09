
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

select * from MY_FAVORITE order by no
delete from my_favorite where no> 25
select * from MY_ANALISYS where news_code = 'digital' group by news_code 
select * from news_count

		
