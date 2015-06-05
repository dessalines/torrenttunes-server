CREATE VIEW search_view AS 
select *, album || ' - ' || artist || ' - ' || title as search
from song;


select * from search_view where 
search like '%nine%' and
search like '%era%';