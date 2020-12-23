CREATE TABLE public.precomputed_mags (
    accession varchar(255) NOT NULL,
    species varchar(255) NULL,
    marker bool NOT NULL DEFAULT false,
    score float8 NULL,
    z_score float8 NULL,
    diso float8 NULL,
    abd float8 NULL,
    csl float8 NULL,
    "int" int4 NULL,
    len int4 NULL,
    max int4 NULL,
    phs int4 NULL,
    pip float8 NULL,
    rna int4 NULL,
    mrf int4 NULL,
    lps float8 NULL,
    cat float8 NULL,
    tgo float8 NULL,
    gvy float8 NULL,
    a float8 NULL,
    c float8 NULL,
    d float8 NULL,
    e int4 NULL,
    f float8 NULL,
    g float8 NULL,
    h int4 NULL,
    i float8 NULL,
    k float8 NULL,
    l float8 NULL,
    m float8 NULL,
    n float8 NULL,
    p float8 NULL,
    q float8 NULL,
    r float8 NULL,
    s float8 NULL,
    t float8 NULL,
    v float8 NULL,
    w float8 NULL,
    y int4 NULL,
    CONSTRAINT precomputed_mags_pkey PRIMARY KEY (accession)
);

insert into precomputed_mags(accession, diso, abd, csl, "int", len, "max", phs, pip, rna, mrf, lps, cat, a, c, d, e, f, g, h, i, k, l, m, n, p, q, r, s, t, v, w, y) 
select accession, diso, abd, csl, "int", len, "max", phs, pip, rna, mrf, lps, cat, a, c, d, e, f, g, h, i, k, l, m, n, p, q, r, s, t, v, w, y  from precomp_human_data_tsv

UPDATE precomputed_mags set species='HUMAN';

insert into precomputed_mags(accession,diso,abd,csl,"int",len,"max",phs,pip,rna,mrf,lps,cat,tgo,gvy,a,c,d,e,f,g,h,i,k,l,m,n,p,q,r,s,t,v,w,y) 
select accession,diso,abd,csl,"int",len,"max",phs,pip,rna,mrf,lps,cat,tgo,gvy,a,c,d,e,f,g,h,i,k,l,m,n,p,q,r,s,t,v,w,y from precomp_yeast_data_tsv

UPDATE precomputed_mags set species='YEAST' where species is NULL;

UPDATE precomputed_mags set marker = true where accession in ('P04147', 'Q13283', 'Q12517', 'Q9NPI6', 'Q8IU60', 'P53550', 'Q03465', 'P18583', 'P15646', 'O00567')

update precomputed_mags set z_score = "avg" from precomp_human_zs_tsv where precomp_human_zs_tsv.nam = precomputed_mags.accession;
update precomputed_mags set z_score = "avg" from precomp_yeast_zs_tsv where precomp_yeast_zs_tsv."name" = precomputed_mags.accession;

CREATE TABLE public.precomputed_magsseq (
    accession varchar(255) NOT NULL,
    species varchar(255) NULL,
    marker bool NOT NULL DEFAULT false,
    score float8 NULL,
    z_score_human float8 NULL,
    z_score_yeast float8 NULL,
    diso float4 NULL,
    len int4 NULL,
    run int4 NULL,
    max int4 NULL,
    chg float4 NULL,
    net float4 NULL,
    gvy float4 NULL,
    pip float4 NULL,
    tgo float4 NULL,
    mfc float4 NULL,
    sto int4 NULL,
    stc int4 NULL,
    sft float4 NULL,
    scn float4 NULL,
    sbb float4 NULL,
    pol float4 NULL,
    rbp float4 NULL,
    sol float4 NULL,
    cat float4 NULL,
    r float4 NULL,
    h float4 NULL,
    k float4 NULL,
    d float4 NULL,
    e float4 NULL,
    s float4 NULL,
    t float4 NULL,
    n float4 NULL,
    q float4 NULL,
    c float4 NULL,
    g float4 NULL,
    p float4 NULL,
    a float4 NULL,
    v float4 NULL,
    i float4 NULL,
    l float4 NULL,
    m float4 NULL,
    f float4 NULL,
    y float4 NULL,
    w float4 null,
    CONSTRAINT precomputed_magsseq_pkey PRIMARY KEY (accession)
);

insert into precomputed_magsseq(accession,diso,len,run,"max",chg,net,gvy,pip,tgo,mfc,sto,stc,rbp,sol,cat,r,h,k,d,e,s,t,n,q,c,g,p,a,v,i,l,m,f,y,w) 
select nam,dis,len,run,"max",chg,net,gvy,pip,tgo,mfc,sto,stc,rbp,sol,cat,r,h,k,d,e,s,t,n,q,c,g,p,a,v,i,l,m,f,y,w  from seq_hmn_pme_corr_txt

UPDATE precomputed_magsseq set species='HUMAN';

insert into precomputed_magsseq(accession,diso,len,run,"max",chg,net,gvy,pip,tgo,mfc,sto,stc,sft,scn,sbb,pol,rbp,sol,cat,r,h,k,d,e,s,t,n,q,c,g,p,a,v,i,l,m,f,y,w) 
select nam,dis,len,run,"max",chg,net,gvy,pip,tgo,mfc,sto,stc,sft,scn,sbb,pol,rbp,sol,cat,r,h,k,d,e,s,t,n,q,c,g,p,a,v,i,l,m,f,y,w from seq_yst_pme_corr_txt

UPDATE precomputed_magsseq set species='YEAST' where species is NULL;

update precomputed_magsseq set z_score_human = zs from hmn_zs_tsv where hmn_zs_tsv.accession = precomputed_magsseq.accession;
update precomputed_magsseq set z_score_yeast = zs from yst_zs_tsv where yst_zs_tsv.accession = precomputed_magsseq.accession;


-- Markers
CREATE TABLE public.marker_mags (
    accession varchar(255) NOT NULL,
    label varchar(255) NOT NULL,
    CONSTRAINT marker_mags_pkey PRIMARY KEY (accession),
    CONSTRAINT marker_mags_fkey FOREIGN KEY (accession) REFERENCES precomputed_mags(accession)
);
INSERT INTO marker_mags (accession, label) VALUES
    ('Q12517', 'p-body (DCP1)'),
    --    ('Q9NPI6', 'p-body (DCP1)'),
    ('Q8IU60', 'p-body (DCP2)'),
    ('P53550', 'p-body (DCP2)'),
    ('P04147', 'Stress Granule (PAB1)'),
    ('P11940', 'Stress Granule (PAB1)'),
    ('Q13283', 'Stress Granule (G3BP1)');

CREATE TABLE public.marker_magseq (
    accession varchar(255) NOT NULL,
    label varchar(255) NOT NULL,
    CONSTRAINT marker_magseq_pkey PRIMARY KEY (accession),
    CONSTRAINT marker_magseq_fkey FOREIGN KEY (accession) REFERENCES precomputed_magsseq(accession)
);
INSERT INTO marker_magseq (accession, label) VALUES
('Q12517', 'p-body (DCP1)'),
('Q9NPI6', 'p-body (DCP1)'),
('Q8IU60', 'p-body (DCP2)'),
('P53550', 'p-body (DCP2)'),
('P04147', 'Stress Granule (PAB1)'),
('P11940', 'Stress Granule (PAB1)'),
('Q13283', 'Stress Granule (G3BP1)');

-- Indices

CREATE INDEX job_input_idx ON public.job USING btree (input);