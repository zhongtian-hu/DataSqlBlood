select
	a.contract_id ,
	(case
		when (a.contract_id = 'FZ-BX-2013(02)') then '-1'
		else a.value_code
	end) value_code ,
	coalesce(a.sign_date,
	a.start_date) sign_date ,
	(case
		when (a.end_date in ('20991231',
		'20960922',
		'20990829',
		'20680830')) then U & '\4E0D\786E\5B9A'
		when ((not (a.end_date in ('20991231',
		'20960922',
		'20990829',
		'20680830')))
		and (("datediff"("date_parse"(a.end_date,
		'%Y%m%d'),
		"date_parse"(coalesce(
		(
			case a.start_date
			when '19990101' then a.sign_date
			else a.start_date
		end),
		a.sign_date),
		'%Y%m%d')) / 360) = 0)) then U & '1\5E74'
		when ((not (a.end_date in ('20991231',
		'20960922',
		'20990829',
		'20680830')))
		and (("datediff"("date_parse"(a.end_date,
		'%Y%m%d'),
		"date_parse"(coalesce(
		(
			case a.start_date
			when '19990101' then a.sign_date
			else a.start_date
		end),
		a.sign_date),
		'%Y%m%d')) / 360) > 0)) then "concat"(cast(("datediff"("date_parse"(a.end_date,
		'%Y%m%d'),
		"date_parse"(coalesce(
		(
			case a.start_date
			when '19990101' then a.sign_date
			else a.start_date
		end),
		a.sign_date),
		'%Y%m%d')) / 360) as varchar),
		U & '\5E74')
		else U & '\4E0D\786E\5B9A'
	end) date_range ,
	coalesce(
	(
		case a.start_date
		when '19990101' then a.sign_date
		else a.start_date
	end),
	a.sign_date) start_date ,
	a.end_date ,
	a.plan_name ,
	coalesce(a.fund_abbr,
	a.contract_id) fund_abbr ,
	a.prod_type ,
	a.mgr_type ,
	a.is_graded ,
	a.lever_ratio ,
	a.mgr_company ,
	a.admin_ratio ,
	a.admin_days ,
	a.admin_fare_freq ,
	a.admin_fare_type ,
	a.fare_owner ,
	a.calc_type ,
	a.is_amortize ,
	a.trustee ,
	a.trustee_org ,
	A.trust_fee ,
	a.entrust ,
	a.is_asso ,
	coalesce(b.fund_code,
	a.fund_code) fund_code ,
	coalesce(b.record_date,
	a.sign_date) CSBARQ ,
	a.jhlx ,
	coalesce(fin_Consultant,
	' ') fin_Consultant ,
	coalesce(cast(a.data_status as varchar),
	'') data_status ,
	a.clr_date ,
	a.gpzyl ,
	a.fjbl ,
	a.logdate
from
	((
	select
		a.contract_id ,
		fund_code ,
		value_code ,
		sign_date ,
		start_date ,
		end_date ,
		a.plan_name ,
		fund_abbr ,
		prod_type ,
		mgr_type ,
		is_graded ,
		lever_ratio ,
		mgr_company ,
		coalesce(c.glfl,
		admin_ratio) admin_ratio ,
		admin_days ,
		admin_fare_freq ,
		admin_fare_type ,
		a.fare_owner ,
		a.calc_type ,
		a.is_amortize ,
		coalesce(b.name,
		a.trustee) trustee ,
		b.org_code trustee_org ,
		(case
			when (coalesce(b.ratio,
			0) > 0) then b.ratio
			else c.tgfl
		end) trust_fee ,
		entrust ,
		is_asso ,
		'' fin_org ,
		'' law_org ,
		'' fin_Consultant ,
		'1' JHLX ,
		data_status ,
		a.clr_date ,
		a.logdate ,
		coalesce(c.gpzyl,
		0) gpzyl ,
		0 fjbl
	from
		((ods19.tb_direct_contract a
	left join ods19.tb_direct_contract_trustee b on
		(((a.contract_id = b.contract_id)
		and (a.logdate = b.logdate))
		and (b.flag <> 3)))
	left join (
		select
			contract_id ,
			a.logdate ,
			("sum"(
			(
				case fare_type
				when 1 then fare_ratio
			end)) / "count"(distinct a.product_id)) glfl ,
			("sum"(
			(
				case fare_type
				when 2 then fare_ratio
			end)) / "count"(distinct a.product_id)) tgfl ,
			"sum"(
			(
				case
				when (a.holding_table = 36) then 1
				else 0
			end)) gpzyl
		from
			ods19.tb_direct_product_info a ,
			ods19."TB_PRODUCT_RATE" b
		where
			(((b.fare_type in (1,
			2))
			and (b.flag <> 3))
			and (a.product_id = b.product_id))
		group by
			contract_id,
			a.logdate ) c on
		(a.contract_id = c.contract_id))
	where
		((a.flag <> 3)
		and (((a.end_date >= a.logdate)
		or (a.end_date is null))
		or (clr_date is null)))
union
	select
		a.contract_id ,
		fund_code ,
		value_code ,
		coalesce(sign_date,
		start_date) ,
		start_date ,
		coalesce(end_date,
		'20991231') end_date ,
		a.plan_name ,
		a.fund_attr ,
		prod_type ,
		mgr_type ,
		is_grade ,
		cast(lever_ratio as decimal(19,
		6)) ,
		mgr_company ,
		(case
			when (c.glfl is not null) then c.glfl
			when ((c.glfl is null)
			and (admin_ratio is not null)) then (cast(admin_ratio as decimal(19,
			6)) / 100)
			else 0
		end) admin_ratio ,
		admin_days ,
		admin_fare_freq ,
		admin_fare_type ,
		a.fare_owner ,
		a.calc_type ,
		a.is_amortize ,
		coalesce(b.name,
		a.trustee) trustee ,
		b.org_code ,
		(case
			when (c.tgfl is not null) then c.tgfl
			else 0
		end) trust_fee ,
		entrust ,
		is_asso ,
		'' ,
		'' ,
		'' ,
		'2' JHLX ,
		data_status ,
		a.clr_date ,
		a.logdate ,
		coalesce(d.gpzyl,
		0) ,
		coalesce(d.fjbl,
		0)
	from
		(((ods19.tb_collection_contract a
	left join ods19.tb_direct_contract_trustee b on
		(((a.contract_id = b.contract_id)
		and (a.logdate = b.logdate))
		and (b.flag <> 3)))
	left join ods08."vw_wbfk_LFFLSZ" c on
		((a.value_code = cast(c.l_ztbh as varchar))
		and (c.logdate = a.logdate)))
	left join (
		select
			"replace"("substring"(d_ywrq,
			1,
			10),
			'-',
			'') LOGDATE ,
			L_ZTBH ,
			(case
				when ("sum"(
				(
					case
					when (vc_kmdm like U & '\5B9E\6536\8D44\672C\91D1\989D%\98CE\9669\7EA7') then en_sz
					else 0
				end)) > 0) then "round"((("sum"(
				(
					case
					when (vc_kmdm = U & '\5B9E\6536\8D44\672C\91D1\989D') then en_sz
					else 0
				end)) - "sum"(
				(
					case
					when (vc_kmdm like U & '\5B9E\6536\8D44\672C\91D1\989D%\98CE\9669\7EA7') then en_sz
					else 0
				end))) / "sum"(
				(
					case
					when (vc_kmdm like U & '\5B9E\6536\8D44\672C\91D1\989D%\98CE\9669\7EA7') then en_sz
					else 0
				end))),
				2)
				else 0
			end) fjbl ,
			"sum"(
			(
				case
				when (vc_kmdm in ('12020203',
				'12020103')) then 1
				else 0
			end)) gpzyl
		from
			ods08.vjk_wbfk_gzb
		where
			((vc_kmdm like U & '\5B9E\6536\8D44\672C\91D1\989D%\98CE\9669\7EA7')
			or (vc_kmdm in (U & '\5B9E\6536\8D44\672C\91D1\989D',
			'12020203',
			'12020103')))
		group by
			"replace"("substring"(d_ywrq,
			1,
			10),
			'-',
			''),
			L_ZTBH
		having
			("sum"(
			(
				case
				when ((vc_kmdm like U & '\5B9E\6536\8D44\672C\91D1\989D%\98CE\9669\7EA7')
				or (vc_kmdm in ('12020203',
				'12020103'))) then en_sz
				else 0
			end)) > 0) ) d on
		((a.value_code = cast(d.l_ztbh as varchar))
		and (d.logdate = a.logdate)))
	where
		((a.flag <> 3)
		and (((end_date >= a.logdate)
		or (end_date is null))
		or (clr_date is null))) ) a
left join ods19."TB_CONTRACT_BACKUP" b on
	(((b.logdate = a.logdate)
	and (b.flag <> 3))
	and (a.contract_id = b.contract_id)))
where
	(a.logdate >= '20190701');