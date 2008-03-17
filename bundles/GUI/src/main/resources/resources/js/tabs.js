var cobj;

function getHash(offset) {
	var href = document.location.href;
	var i = href.indexOf( "#" );
	return i >= 0 ? href.substr( i + 1 + offset ) : null;
}

function initTabs(d) {
	var i, obj = document.getElementsByTagName('fieldset');
	for(i = 0; i < obj.length; i++) {
		if (obj[i].id.substring(0,1) == 'd') {
			obj[i].style.display = 'none';
			obj[i].getElementsByTagName('legend')[0].style.display = 'none';
		}
	}
	tab = getHash(1);
	fshow(tab ? tab : d);
	setInterval( function() {
		var newTab = getHash(1);
		if (newTab != tab) {
			fshow(newTab ? newTab : d);
			tab = newTab;
		}
	}, 200);
}

function fshow(n) {		
	if(cobj != null) {
		document.getElementById('d' + cobj).style.display = 'none';
		document.getElementById('t' + cobj).className = '';
	}
	cobj = n;
	document.getElementById('d' + n).style.display = '';
	document.getElementById('t' + n).className = 'active';		
	return false;
}