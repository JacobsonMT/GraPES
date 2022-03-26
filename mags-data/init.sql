
insert into precomputed_mags(accession, diso, abd, csl, "int", len, "max", phs, pip, rna, mrf, lps, cat, a, c, d, e, f, g, h, i, k, l, m, n, p, q, r, s, t, v, w, y)
select "name", diso, abd, csl, "int", len, "max", phs, pip, rna, mrf, lps, cat, a, c, d, e, f, g, h, i, k, l, m, n, p, q, r, s, t, v, w, y  from precomp_human_data_tsv;

UPDATE precomputed_mags set species='HUMAN';

insert into precomputed_mags(accession,diso,abd,csl,"int",len,"max",phs,pip,rna,mrf,lps,cat,tgo,gvy,a,c,d,e,f,g,h,i,k,l,m,n,p,q,r,s,t,v,w,y) 
select "name",diso,abd,csl,"int",len,"max",phs,pip,rna,mrf,lps,cat,tgo,gvy,a,c,d,e,f,g,h,i,k,l,m,n,p,q,r,s,t,v,w,y from precomp_yeast_data_tsv;

UPDATE precomputed_mags set species='YEAST' where species is NULL;

update precomputed_mags set z_score = "avg" from precomp_human_zs_tsv where precomp_human_zs_tsv.nam = precomputed_mags.accession;
update precomputed_mags set z_score = "avg" from precomp_yeast_zs_tsv where precomp_yeast_zs_tsv."name" = precomputed_mags.accession;

--  gene names

update precomputed_mags SET gene = "human_uni2genesyn.txt".gene from "human_uni2genesyn.txt" where "human_uni2genesyn.txt".accession=precomputed_mags.accession;
update precomputed_mags SET gene = "yeast_gene.txt".gene from "yeast_gene.txt" where "yeast_gene.txt".accession=precomputed_mags.accession;

--  magseq

insert into precomputed_magsseq(accession,diso,len,run,"max",chg,net,gvy,pip,tgo,mfc,sto,stc,rbp,sol,cat,r,h,k,d,e,s,t,n,q,c,g,p,a,v,i,l,m,f,y,w) 
select nam,dis,len,run,"max",chg,net,gvy,pip,tgo,mfc,sto,stc,rbp,sol,cat,r,h,k,d,e,s,t,n,q,c,g,p,a,v,i,l,m,f,y,w  from tmp_seq_hmn;

UPDATE precomputed_magsseq set species='HUMAN';

insert into precomputed_magsseq(accession,diso,len,run,"max",chg,net,gvy,pip,tgo,mfc,sto,stc,sft,scn,sbb,pol,rbp,sol,cat,r,h,k,d,e,s,t,n,q,c,g,p,a,v,i,l,m,f,y,w) 
select nam,dis,len,run,"max",chg,net,gvy,pip,tgo,mfc,sto,stc,sft,scn,sbb,pol,rbp,sol,cat,r,h,k,d,e,s,t,n,q,c,g,p,a,v,i,l,m,f,y,w from tmp_seq_yst;

UPDATE precomputed_magsseq set species='YEAST' where species is NULL;

-- Scores

update precomputed_magsseq set z_score_human = avg from tmp_scores_hmn where tmp_scores_hmn.nam = precomputed_magsseq.accession;
update precomputed_magsseq set z_score_yeast = avg from tmp_scores_yst where tmp_scores_yst.nam = precomputed_magsseq.accession;

--  gene names
update precomputed_magsseq SET gene = "human_uni2genesyn.txt".gene from "human_uni2genesyn.txt" where "human_uni2genesyn.txt".accession=precomputed_magsseq.accession;
update precomputed_magsseq SET gene = "yeast_gene.txt".gene from "yeast_gene.txt" where "yeast_gene.txt".accession=precomputed_magsseq.accession;

-- Markers

INSERT INTO marker_mags (accession, label) VALUES
    ('Q12517', 'p-body (DCP1)'),
    --    ('Q9NPI6', 'p-body (DCP1)'),
    ('Q8IU60', 'p-body (DCP2)'),
    ('P53550', 'p-body (DCP2)'),
    ('P04147', 'Stress Granule (PAB1)'),
    ('P11940', 'Stress Granule (PAB1)'),
    ('Q13283', 'Stress Granule (G3BP1)');


INSERT INTO marker_magseq (accession, label) VALUES
('Q12517', 'p-body (DCP1)'),
('Q9NPI6', 'p-body (DCP1)'),
('Q8IU60', 'p-body (DCP2)'),
('P53550', 'p-body (DCP2)'),
('P04147', 'Stress Granule (PAB1)'),
('P11940', 'Stress Granule (PAB1)'),
('Q13283', 'Stress Granule (G3BP1)');


ALTER TABLE public.precomputed_mags ADD sg bool NULL DEFAULT false;
COMMENT ON COLUMN public.precomputed_mags.sg IS 'Known SG protein';

update precomputed_mags set sg=true where species = 'HUMAN' and accession in (
    'A0AV96','A1KXE4','A1L020','A5YKK6','C9JLW8','O00303','O00425','O00505','O00571','O14578','O14787','O14979','O15063','O15067','O15116','O15226','O15234','O15371','O15372','O15523','O43303','O43347','O43432','O43781','O60306','O60333','O60506','O60573','O60716','O60739','O60930','O75175','O75179','O75340','O75369','O75420','O75506','O75534','O75569','O75821','O75822','O75880','O76003','O94901','O95319','O95416','O95429','O95628','O95758','O95793','O95985','P02511','P04080','P04083','P04792','P05114','P05165','P05198','P06730','P06748','P07237','P07951','P09651','P0CB38','P0DPI2','P11940','P13489','P15170','P16989','P17096','P18754','P19525','P20073','P22061','P22105','P22626','P23528','P23588','P26196','P26378','P26599','P27348','P27695','P27816','P29558','P30101','P30154','P30626','P30876','P31483','P31942','P31943','P31948','P33992','P33993','P35249','P35606','P35611','P35637','P37802','P38159','P38432','P40227','P42694','P43304','P43487','P46013','P46060','P46109','P47755','P48634','P49368','P49411','P49750','P49756','P50479','P50552','P50579','P50995','P51114','P51116','P51648','P51784','P51991','P52272','P52292','P52294','P52597','P52701','P52948','P53992','P54727','P55060','P55265','P55795','P55884','P56192','P56537','P58546','P60228','P60510','P60520','P60842','P60981','P61019','P61758','P62633','P62826','P62993','P62995','P63162','P63241','P63244','P67809','P68366','P78312','P78344','P83916','P84103','P98179','Q00577','Q00796','Q01085','Q01804','Q02952','Q04637','Q04760','Q06787','Q06830','Q07666','Q07955','Q12849','Q12874','Q12926','Q12933','Q12986','Q12988','Q13155','Q13162','Q13185','Q13242','Q13243','Q13283','Q13285','Q13310','Q13347','Q13427','Q13442','Q13642','Q14011','Q14151','Q14152','Q14157','Q14201','Q14240','Q14247','Q14258','Q14444','Q14493','Q14576','Q14671','Q14677','Q14694','Q14847','Q14966','Q14978','Q15004','Q15005','Q15032','Q15056','Q15274','Q15365','Q15366','Q15424','Q15434','Q15436','Q15555','Q15637','Q15654','Q15717','Q15785','Q16514','Q16555','Q16630','Q16637','Q16740','Q17RY0','Q2TAY7','Q3MHD2','Q53EP0','Q53GS7','Q5JSZ5','Q5PRF9','Q5SZQ8','Q5T5Y3','Q5T6F2','Q5TAX3','Q5TC82','Q5TF21','Q5VZF2','Q5W0B1','Q659C4','Q6IA86','Q6NYC8','Q6P158','Q6P1Q9','Q6P2E9','Q6P2H3','Q6P9H4','Q6UW02','Q6Y7W6','Q6ZN04','Q6ZN17','Q702N8','Q71RC2','Q71UI9','Q7KZF4','Q7L099','Q7L2E3','Q7L2H7','Q7Z2W4','Q7Z417','Q7Z739','Q86SQ0','Q86TB9','Q86TG7','Q86UK7','Q86US8','Q86UW6','Q86V48','Q86X55','Q86XN8','Q86XZ4','Q86Y13','Q8IU60','Q8IWR0','Q8IWZ3','Q8IX01','Q8IX12','Q8IXT5','Q8IZD4','Q8IZH2','Q8N3C0','Q8N543','Q8N9N2','Q8NC51','Q8NCA5','Q8ND24','Q8ND56','Q8NDC0','Q8NDV7','Q8NE71','Q8NFA0','Q8NHU6','Q8TB72','Q8TDB6','Q8TEP8','Q8TF46','Q8WU79','Q8WUA4','Q8WWM7','Q8WX93','Q8WXF1','Q8WYQ9','Q92499','Q92540','Q92545','Q92567','Q92575','Q92600','Q92615','Q92628','Q92804','Q92844','Q92879','Q92900','Q92901','Q92945','Q92973','Q92997','Q93052','Q969T9','Q96A57','Q96A72','Q96AE4','Q96AE7','Q96C10','Q96CX2','Q96DH6','Q96EP5','Q96F86','Q96HC4','Q96I24','Q96IZ0','Q96LI5','Q96PU8','Q96PV7','Q96QR8','Q96QZ7','Q96RY5','Q96T21','Q99417','Q99439','Q99459','Q99613','Q99615','Q99698','Q99700','Q99729','Q99873','Q99959','Q9BQ04','Q9BQ61','Q9BRZ2','Q9BSJ8','Q9BTT0','Q9BUJ2','Q9BUT9','Q9BWF3','Q9BX40','Q9BY12','Q9BY44','Q9BYJ9','Q9BZB8','Q9BZI7','Q9C0B9','Q9C0C2','Q9H000','Q9H019','Q9H0D6','Q9H0E9','Q9H0J9','Q9H0L4','Q9H0Z9','Q9H171','Q9H2U1','Q9H361','Q9H3G5','Q9H3U7','Q9H4A3','Q9H694','Q9H6S0','Q9H6Z4','Q9H773','Q9H7E2','Q9H840','Q9H910','Q9H9A5','Q9H9Z2','Q9HAU5','Q9HBD1','Q9HC16','Q9HCC0','Q9HCE1','Q9HCJ0','Q9HCM7','Q9NP73','Q9NPI1','Q9NPI6','Q9NQP4','Q9NR30','Q9NR56','Q9NRA8','Q9NRC1','Q9NRR4','Q9NTJ3','Q9NUD5','Q9NUL3','Q9NX05','Q9NYF8','Q9NYL9','Q9NZB2','Q9NZI8','Q9NZN8','Q9P0V3','Q9P258','Q9P287','Q9P2D0','Q9P2K5','Q9UBE0','Q9UBN7','Q9UBV8','Q9UFF9','Q9UGR2','Q9UI15','Q9UIV1','Q9UKF6','Q9UKV8','Q9UKY7','Q9UKZ1','Q9UL18','Q9ULM3','Q9ULM6','Q9ULV4','Q9ULX6','Q9UN86','Q9UNF1','Q9UNW9','Q9UNZ2','Q9UPQ9','Q9Y224','Q9Y262','Q9Y266','Q9Y2K5','Q9Y2U8','Q9Y2Z0','Q9Y314','Q9Y3B9','Q9Y3F4','Q9Y3I0','Q9Y4F3','Q9Y508','Q9Y520','Q9Y561','Q9Y5A9','Q9Y5V3','Q9Y6M1'
);

update precomputed_mags set sg=true where species = 'YEAST' and accession in (
'P00127','P00546','P00549','P00812','P00815','P00899','P00912','P00942','P00950','P00958','P02992','P03872','P04050','P04147','P04801','P04840','P05453','P06102','P06103','P06105','P06634','P06786','P07258','P07259','P07260','P07262','P07264','P07283','P07284','P07703','P08018','P08432','P08518','P08536','P09032','P0CX33','P10080','P10081','P10591','P10592','P10659','P11745','P11792','P11938','P11972','P12385','P12688','P12754','P13188','P13382','P13433','P14540','P15180','P15454','P15565','P15891','P15992','P16140','P16521','P16550','P16892','P17967','P19097','P19358','P19524','P20435','P20447','P20448','P20459','P20485','P21304','P22147','P22211','P22336','P22696','P23201','P23394','P23594','P24276','P24280','P24783','P25294','P25555','P25558','P25567','P25623','P25631','P25655','P26754','P27476','P28272','P28274','P29295','P29311','P29547','P30771','P31384','P31539','P32263','P32288','P32324','P32461','P32471','P32497','P32527','P32558','P32582','P32588','P32597','P32644','P32657','P32770','P32831','P32861','P32892','P32895','P32898','P32905','P32909','P33201','P33297','P33309','P33399','P33892','P34167','P34241','P34253','P34760','P34761','P34909','P35194','P35691','P35844','P36008','P36010','P36013','P36041','P36049','P36069','P36421','P36775','P38009','P38074','P38143','P38144','P38199','P38219','P38249','P38263','P38323','P38333','P38431','P38623','P38627','P38689','P38691','P38715','P38747','P38764','P38788','P38817','P38822','P38828','P38873','P38911','P38912','P38934','P38966','P38996','P38999','P39517','P39533','P39676','P39729','P39730','P39935','P39936','P39940','P39960','P39969','P39976','P39998','P40007','P40010','P40013','P40024','P40029','P40069','P40150','P40157','P40160','P40210','P40217','P40422','P40469','P40484','P40545','P40564','P40825','P40850','P40991','P41277','P41810','P41832','P42842','P42945','P43535','P43583','P43593','P43609','P45978','P46655','P46672','P46683','P47017','P47077','P47120','P47160','P47169','P47176','P47912','P48362','P48567','P49017','P50109','P52910','P53043','P53091','P53111','P53131','P53145','P53183','P53200','P53261','P53276','P53295','P53297','P53316','P53327','P53550','P53686','P53741','P53742','P53829','P53834','P53847','P53883','P53894','P53905','P53909','P53914','P53924','P54115','P54861','P57743','P69771','Q00402','Q00955','Q01159','Q01477','Q01560','Q01662','Q02256','Q02892','Q03016','Q03264','Q03305','Q03532','Q03690','Q03735','Q03774','Q03834','Q03940','Q03973','Q04067','Q04225','Q04373','Q04432','Q04439','Q04636','Q04660','Q05022','Q05775','Q06103','Q06205','Q06252','Q06336','Q06338','Q06344','Q06505','Q06697','Q07362','Q07551','Q07623','Q07657','Q08208','Q08287','Q08421','Q08444','Q08601','Q08686','Q08925','Q08962','Q08972','Q08979','Q08992','Q12019','Q12034','Q12055','Q12069','Q12072','Q12136','Q12178','Q12285','Q12514','Q12517','Q12532','Q12754','Q3E842','Q99207','Q99383'
);