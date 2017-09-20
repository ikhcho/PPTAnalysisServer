
select * from RT_ANALISYS order by no
select * from company order by no
delete from company where name = '현대모비스'
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
