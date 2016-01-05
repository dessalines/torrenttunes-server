cc197bad-dc9c-440d-a5b5-d52ba2e14234

curl localhost:8080/remove_artist_server/cc197bad-dc9c-440d-a5b5-d52ba2e14234
curl localhost:8080/remove_artist/cc197bad-dc9c-440d-a5b5-d52ba2e14234

curl localhost:4568/remove_artist_server/cc197bad-dc9c-440d-a5b5-d52ba2e14234
curl localhost:4568/remove_artist/cc197bad-dc9c-440d-a5b5-d52ba2e14234

curl -X POST localhost:8080/clear_cache

87d456b2e0c6886ccdaae6f2e463578e

select 
uploader_ip_hash, 
count(*),
sum(plays), 
sum(timeouts),
max(seeders)
from song
-- where uploader_ip_hash is NULL
group by uploader_ip_hash
order by count(*) desc;


select artist.mbid, release_group.mbid, song_release_group.id, song.mbid, song.title,
uploader_ip_hash
from artist
inner join release_group
on artist.mbid = release_group.artist_mbid
inner join song_release_group
on release_group.mbid = song_release_group.release_group_mbid
inner join song 
on song_release_group.song_mbid = song.mbid
where uploader_ip_hash='f457fb8e6fb4c43fa3bfbcfeeb7ec529'
group by release_group.mbid

select release_group.mbid,uploader_ip_hash
from artist
inner join release_group
on artist.mbid = release_group.artist_mbid
inner join song_release_group
on release_group.mbid = song_release_group.release_group_mbid
inner join song 
on song_release_group.song_mbid = song.mbid
where uploader_ip_hash='f457fb8e6fb4c43fa3bfbcfeeb7ec529'
group by release_group.mbid

select distinct uploader_ip_hash from song;

select count(*) from song where uploader_ip_hash='87d456b2e0c6886ccdaae6f2e463578e';

-- These two are for removing songs by bad uploaders
delete srg
from song_release_group srg
inner join song 
on srg.song_mbid = song.mbid 
where uploader_ip_hash='87d456b2e0c6886ccdaae6f2e463578e';

delete 
from song
where uploader_ip_hash='87d456b2e0c6886ccdaae6f2e463578e';


-- delete empty release groups

delete rg 
from release_group rg
inner join 
(
	select release_group.mbid
	from release_group
	left join song_release_group
	on release_group.mbid = song_release_group.release_group_mbid
	left join song 
	on song_release_group.song_mbid = song.mbid
	group by release_group.mbid
	having count(song.mbid) = 0
) b
on rg.mbid = b.mbid;




-- delete empty artists

-- First delete the tag_infos for empty artists:
delete ti
from tag_info ti
inner join
(
	select artist.mbid
	from artist
	left join release_group
	on artist.mbid = release_group.artist_mbid
	left join song_release_group
	on release_group.mbid = song_release_group.release_group_mbid
	left join song 
	on song_release_group.song_mbid = song.mbid
	group by artist.mbid
	having count(song.mbid) = 0
) a
on ti.artist_mbid = a.mbid;


delete art
from artist art
inner join
(
	select artist.mbid
	from artist
	left join release_group
	on artist.mbid = release_group.artist_mbid
	left join song_release_group
	on release_group.mbid = song_release_group.release_group_mbid
	left join song 
	on song_release_group.song_mbid = song.mbid
	group by artist.mbid
	having count(song.mbid) = 0
) a
on art.mbid = a.mbid;


-- check to see which artists need their columns converted
select *
from artist 
where artist.name like '%?%';



-- An indicator that you need to fix song_view, because its not finding this song
-- from jeremy irons on a disney mix cd
select artist.mbid, release_group.mbid, song_release_group.id, song.mbid, song.title
from artist
left join release_group
on artist.mbid = release_group.artist_mbid
left join song_release_group
on release_group.mbid = song_release_group.release_group_mbid
left join song 
on song_release_group.song_mbid = song.mbid
where artist.mbid = '349ab9cc-3062-430e-9561-d73d0e10e08a'
group by artist.mbid
having count(song.mbid) = 0
