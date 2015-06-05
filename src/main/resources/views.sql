CREATE VIEW search_view AS 
select *, album || ' - ' || artist || ' - ' || title as search
from song;


select * from search_view where 
search like '%guided%' and
search like '%gold%';

select * from search_view where 
search like '%gold%' and
search like '%guided%';


select * from search_view where
search REGEXP "^.*(Dow).*$";