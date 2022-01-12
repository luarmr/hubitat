Array.from(document.querySelectorAll('.nameDiv a'))
    .filter(el => el.innerHTML==="Rule Machine" ||  el.innerHTML==="Rule Machine Legacy" )
    .reduce((acc, el) => acc.concat( Array.from(el.parentElement.parentElement.parentElement.children)), [] )
    .filter(el => el.className !== "parentNode")
    .forEach(async el => {
        const link = el.querySelector('[href*="installedapp/configure"]'); const rule_id = parseInt(link.href.replace( /^.*\//,''),10);
        const response = await fetch('/installedapp/configure/json/'+rule_id)
        const text = await response.text()
        if (text.indexOf('**Broken Action**') !== -1) {
            el.style.background = 'red';
        } else {
            el.style.background = '#90EE90';
        }
    })
