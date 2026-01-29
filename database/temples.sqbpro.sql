--  select count(*) from temples; 

-- commit;

select count(temple_id) from temples where source = 'a';    -- 12103
select count(temple_id) from temples where source = 's';    -- 318173
-- total = 330276

select * from temples where marked = 'y' and dm is not null and visit_dt is null;

SELECT dm, name, tags, visit_dt FROM  temples where dm = 'kurktykatk';

SELECT  * FROM  temples where marked = 'y';
SELECT  * FROM  temples where dm is not null and visit_dt is null;


SELECT  name,  place  FROM  temples where sthalapuranam = 'y';
	
UPDATE temples
SET tags = REPLACE(tags, 'marriage', 'kalyana-parikaram')
WHERE tags LIKE '%marriage%';

SELECT 
	name,
	deity,
	tags,
	place,
    weight,
	substr(latlong, 1, instr(latlong, ',') - 1) AS lat,
    substr(latlong, instr(latlong, ',') +1) AS long
FROM 
	temples
where 
	marked = 'y';

	UPDATE temples SET old_deity = deity;
	
	update temples set deity = 'shakthi' where deity = 'amman';
	select count(*) from temples where deity not in ( 'shakthi' , 'shiva', 'vishnu', 'ganesh', 'murugan' );
	update temples set deity = 'other' where deity not in ( 'shakthi' , 'shiva', 'vishnu', 'ganesh', 'murugan' ,'nomatch');
	
select name , latlong , deity , tags , place  from temples where marked = 'y' and tags like '%marriage%' or tags like '%health%' or tags like '%wealth%' or tags like '%puthra%'

select name , latlong , deity , tags , place  from temples where complete = 'y' and tags like '%educa%' 

update temples set place = 'doddamallur' where temple_id = 1375805

 ALTER TABLE temples RENAME COLUMN complete TO marked;

 select count(*) from temples where type  is  not null;
 select * from temples where address  is  not null;
 
UPDATE temples set tags = NULL where tags = '';

UPDATE temples SET remark = COALESCE(type, '') || ' || ' || COALESCE(comment, '')

 UPDATE temples SET remark = null where remark = ' || '

UPDATE temples
SET weight = (
    CASE 
        WHEN dm IS NOT NULL AND tags IS NOT NULL THEN 7 + ABS(RANDOM() % 3) 
        WHEN dm IS NOT NULL AND tags IS NULL THEN 4 + ABS(RANDOM() % 3) 
        WHEN dm IS  NULL AND tags IS not NULL THEN 6 + ABS(RANDOM() % 3) 
		else 1
    END
)

-- json for temple weight calc , now using a random number - mar 28,24
-- {
--   "age": 7,
--   "puranic": 4,
--   "contemporary": 3,
--   "size": 5,
--   "architecture": 8,
--   "features": 2
-- }


WITH 
source_a_temples AS (
  SELECT 
    temple_id AS id_1,
    source AS source_1, 
    name AS n1, 
    place AS p1,
    deity AS deity1,
    substr(latlong, 1, instr(latlong, ',') - 1) * 1.0 AS lat1,
    substr(latlong, instr(latlong, ',') + 1) * 1.0 AS long1  
  FROM 
    temples
  WHERE 
    latlong < '13.40'
),

source_b_temples AS (
  SELECT 
    temple_id AS id_2,
    source AS source_2, 
    name AS n2, 
    place AS p2,
    deity AS deity2,
    substr(latlong, 1, instr(latlong, ',') - 1) * 1.0 AS lat2,
    substr(latlong, instr(latlong, ',') + 1) * 1.0 AS long2  
  FROM 
    temples
  WHERE  
    latlong < '13.40'
)

SELECT 
  a.id_1, 
  a.source_1,  
  a.n1, 
  a.p1,
  a.deity1,
  b.id_2,   
  b.source_2,  
  b.n2, 
  b.p2,
  b.deity2,
  -- Calculate distance in meters using Haversine formula
  6371000 * 2 * ASIN(
    SQRT(
      POWER(SIN((RADIANS(b.lat2) - RADIANS(a.lat1)) / 2), 2) +
      COS(RADIANS(a.lat1)) * COS(RADIANS(b.lat2)) *
      POWER(SIN((RADIANS(b.long2) - RADIANS(a.long1)) / 2), 2)
    )
  ) AS distance_meters
FROM source_a_temples a
JOIN source_b_temples b
WHERE 
  -- Distance less than 50 meters
  6371000 * 2 * ASIN(
    SQRT(
      POWER(SIN((RADIANS(b.lat2) - RADIANS(a.lat1)) / 2), 2) +
      COS(RADIANS(a.lat1)) * COS(RADIANS(b.lat2)) *
      POWER(SIN((RADIANS(b.long2) - RADIANS(a.long1)) / 2), 2)
    )
  ) < 50
  -- Add deity match condition (case-insensitive)
  AND LOWER(TRIM(a.deity1)) = LOWER(TRIM(b.deity2))
ORDER BY distance_meters ASC;

select count(temple_id) from temples where  latlong < '14' and deity != 'nomatch'
select *  from temples where  latlong < '14' and deity != 'nomatch'
select *  from temples where  source = 'a' or marked = 'y'

SELECT 
    deity,
    COUNT(*) AS temple_count
FROM 
    temples
WHERE 
--     deity != 'nomatch'
    latlong < '14'   -- north of thirupathi roughly
GROUP BY 
    deity
ORDER BY 
    temple_count DESC;

SELECT 
  count(*)
FROM 
    temples
where 
    deity not in ( 'nomatch' , 'shakthi' , 'other' )
--     and latlong < '17.72'   -- hyderabad roughly
	and latlong < '20' -- upto 60% of maharashtra
--      and latlong < '14'   -- north of thirupathi roughly
	
-- for the temple export to ndmobile.db for mobile app - jun 6,2025
CREATE TABLE mobile_app_temples AS
SELECT 
    temple_id,
    name,
    deity,
    latlong,
    tags,
    place,
    weight,
    visit_dt
FROM 
    temples
where 
    deity not in ( 'nomatch' , 'shakthi' , 'other' )
--     and latlong < '20'   -- hyderabad roughly

 SELECT * FROM temples
                     WHERE CAST(SUBSTR(latlong, 1, INSTR(latlong, ',') - 1) AS REAL) BETWEEN 10.747331949141344 AND 10.834170050858656
                    AND CAST(SUBSTR(latlong, INSTR(latlong, ',') + 1) AS REAL) BETWEEN 79.08453257090083 AND 79.17293382909916
																									  
UPDATE temples
SET weight = 0
select * from temples 
WHERE weight IS NULL;

delete from temples where temple_id in ( 203284,203597,204509,202953,206676,1226152,98028,1229597,1229612,1229629,1229618,1232660,207706,206592,208973,1220980, 1216263,1216264,30915,1201996,1203756,1201999,207957,212099,210829,1177272,212097,1171171,210830);

